package uk.gov.justice.govuknotify;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates {@code POST /v2/notifications/email} on the Gov.UK Notify API for lower environments.
 *
 * <p>Two behaviours, both faithful to the real service so the same request behaves identically with
 * or without the simulator:
 * <ol>
 *   <li><b>Send-time permanent failure</b> — a request whose {@code personalisation} is missing the
 *       required {@code material_url} placeholder is rejected with a real Gov.UK Notify
 *       {@code 400 BadRequestError}. This is exactly what real Notify does, and is what the
 *       {@code ng-asb-test.sh perm-failure} scenario triggers (no fileUri -> no material_url).</li>
 *   <li><b>Delivery outcome</b> — for an accepted request the eventual polled status is driven by the
 *       recipient address, using Gov.UK Notify's own reserved simulator addresses:
 *       {@code perm-fail@simulator.notify} -> {@code permanent-failure},
 *       {@code temp-fail@simulator.notify} -> {@code temporary-failure}, anything else ->
 *       {@code delivered}. The chosen status is stored against the minted notification id
 *       ({@link GovUkNotifyStore}) for the status-poll transformer to replay.</li>
 * </ol>
 */
public class GovUkNotifySendEmailResponseTransformer extends ResponseDefinitionTransformer {

    public static final String GOV_UK_NOTIFY_SEND_EMAIL_RESPONSE_TRANSFORMER = "gov-uk-notify-send-email-response-transformer";

    private static final Logger LOGGER = LoggerFactory.getLogger(GovUkNotifySendEmailResponseTransformer.class);
    private static final String TEMPLATE_URI = "https://api.notifications.service.gov.uk/services/%s/templates/%s";
    private static final String NOTIFICATION_URI = "https://api.notifications.service.gov.uk/v2/notifications/%s";
    private static final String SERVICE_USER_ID = "d43135e4-fff3-45df-9a7e-bc7018a4a589";
    private static final String REQUIRED_PERSONALISATION_KEY = "material_url";

    private static final String PERM_FAIL_ADDRESS = "perm-fail@simulator.notify";
    private static final String TEMP_FAIL_ADDRESS = "temp-fail@simulator.notify";

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {
        final ResponseDefinition authError = GovUkNotifyContract.authError(request);
        if (authError != null) {
            LOGGER.info("Gov.UK Notify simulator: rejecting send with 403 (missing/invalid Authorization Bearer token)");
            return authError;
        }

        final JsonObject body = parseBody(request);

        if (!hasRequiredPersonalisation(body)) {
            LOGGER.info("Gov.UK Notify simulator: request missing '{}' personalisation -> 400 BadRequestError",
                    REQUIRED_PERSONALISATION_KEY);
            return badRequest();
        }

        final String emailAddress = body.getString("email_address", "");
        final String status = statusForRecipient(emailAddress);

        final String notificationId = randomUUID().toString();
        GovUkNotifyStore.saveStatus(notificationId, status);
        LOGGER.info("Gov.UK Notify simulator: accepted email to '{}' as id '{}' (pollable status '{}')",
                emailAddress, notificationId, status);

        return created(notificationId, body);
    }

    private static boolean hasRequiredPersonalisation(final JsonObject body) {
        if (!body.containsKey("personalisation")
                || body.get("personalisation").getValueType() != JsonValue.ValueType.OBJECT) {
            return false;
        }
        final JsonObject personalisation = body.getJsonObject("personalisation");
        if (!personalisation.containsKey(REQUIRED_PERSONALISATION_KEY)) {
            return false;
        }
        // Present and non-null is enough. On the success path NG sets material_url via
        // NotificationClient.prepareUpload(...), which yields a JSON OBJECT (not a string); the
        // perm-failure path omits the key entirely. An explicit null or empty string counts as missing.
        final JsonValue value = personalisation.get(REQUIRED_PERSONALISATION_KEY);
        if (value == null || value.getValueType() == JsonValue.ValueType.NULL) {
            return false;
        }
        if (value.getValueType() == JsonValue.ValueType.STRING) {
            return !((JsonString) value).getString().isEmpty();
        }
        return true;
    }

    private static String statusForRecipient(final String emailAddress) {
        final String recipient = emailAddress == null ? "" : emailAddress.trim().toLowerCase();
        switch (recipient) {
            case PERM_FAIL_ADDRESS:
                return "permanent-failure";
            case TEMP_FAIL_ADDRESS:
                return "temporary-failure";
            default:
                return "delivered";
        }
    }

    private ResponseDefinition created(final String notificationId, final JsonObject requestBody) {
        final String templateId = requestBody.getString("template_id", randomUUID().toString());
        final String reference = requestBody.getString("reference", "");

        final String responseBody = createObjectBuilder()
                .add("id", notificationId)
                .add("reference", reference)
                .add("uri", format(NOTIFICATION_URI, notificationId))
                .add("content", createObjectBuilder()
                        .add("subject", "Gov.UK Notify simulator")
                        .add("body", "Simulated notification"))
                .add("template", createObjectBuilder()
                        .add("id", templateId)
                        .add("version", 1)
                        .add("uri", format(TEMPLATE_URI, SERVICE_USER_ID, templateId)))
                .build().toString();

        return new ResponseDefinitionBuilder()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)
                .build();
    }

    private ResponseDefinition badRequest() {
        final String responseBody = createObjectBuilder()
                .add("errors", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("error", "BadRequestError")
                                .add("message", "Missing personalisation: material_url")))
                .add("status_code", 400)
                .build().toString();

        return new ResponseDefinitionBuilder()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)
                .build();
    }

    private JsonObject parseBody(final Request request) {
        final byte[] raw = request.getBody();
        final String json = (raw == null || raw.length == 0) ? "{}" : new String(raw, StandardCharsets.UTF_8);
        try (JsonReader reader = createReader(new StringReader(json))) {
            return reader.readObject();
        } catch (final RuntimeException e) {
            LOGGER.warn("Gov.UK Notify simulator: could not parse request body, treating as empty", e);
            return createObjectBuilder().build();
        }
    }

    @Override
    public String getName() {
        return GOV_UK_NOTIFY_SEND_EMAIL_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }
}
