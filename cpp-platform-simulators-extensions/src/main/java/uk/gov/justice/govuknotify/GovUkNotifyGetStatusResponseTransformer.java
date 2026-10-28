package uk.gov.justice.govuknotify;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;

import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.JsonObjectBuilder;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates {@code GET /v2/notifications/{id}} on the Gov.UK Notify API — the status poll the client
 * makes after a send. Replays the status recorded against this id (see {@link GovUkNotifyStore}) by
 * {@link GovUkNotifySendEmailResponseTransformer} (the outcome its recipient address selected) or
 * {@link GovUkNotifySendPrecompiledLetterResponseTransformer} (the next status of the letter's
 * lifecycle), answering as an email or a letter accordingly.
 */
public class GovUkNotifyGetStatusResponseTransformer extends ResponseDefinitionTransformer {

    public static final String GOV_UK_NOTIFY_GET_STATUS_RESPONSE_TRANSFORMER = "gov-uk-notify-get-status-response-transformer";

    private static final Logger LOGGER = LoggerFactory.getLogger(GovUkNotifyGetStatusResponseTransformer.class);
    private static final Pattern ID_IN_URL = Pattern.compile("/v2/notifications/([0-9a-fA-F-]{36})");
    private static final String TEMPLATE_URI = "https://api.notifications.service.gov.uk/services/%s/templates/%s";
    private static final String SERVICE_USER_ID = "d43135e4-fff3-45df-9a7e-bc7018a4a589";

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {
        final ResponseDefinition authError = GovUkNotifyContract.authError(request);
        if (authError != null) {
            LOGGER.info("Gov.UK Notify simulator: rejecting status poll with 403 (missing/invalid Authorization Bearer token)");
            return authError;
        }

        final String notificationId = extractId(request.getUrl());
        final GovUkNotifyStore.Polled polled = GovUkNotifyStore.poll(notificationId);
        LOGGER.info("Gov.UK Notify simulator: status poll for {} '{}' -> '{}'",
                polled.getType(), notificationId, polled.getStatus());

        final String now = ZonedDateTime.now(UTC).toString();
        final String templateId = randomUUID().toString();

        final JsonObjectBuilder builder = createObjectBuilder()
                .add("id", notificationId)
                .add("type", polled.getType())
                .add("status", polled.getStatus());
        if (polled.isLetter()) {
            builder.add("line_1", "Provided as PDF")
                    .add("postage", polled.getPostage())
                    .add("body", "")
                    .add("subject", "Pre-compiled PDF");
        } else {
            builder.add("email_address", "simulated@simulator.notify")
                    .add("body", "Simulated notification")
                    .add("subject", "Gov.UK Notify simulator");
        }
        final String body = builder
                .add("template", createObjectBuilder()
                        .add("id", templateId)
                        .add("version", 1)
                        .add("uri", format(TEMPLATE_URI, SERVICE_USER_ID, templateId)))
                .add("created_at", now)
                .add("sent_at", now)
                .add("completed_at", now)
                .build().toString();

        return new ResponseDefinitionBuilder()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(body)
                .build();
    }

    private static String extractId(final String url) {
        final Matcher matcher = ID_IN_URL.matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }

    @Override
    public String getName() {
        return GOV_UK_NOTIFY_GET_STATUS_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }
}
