package uk.gov.justice.govuknotify;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.JsonObject;
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
 * Simulates {@code POST /v2/notifications/letter} for a precompiled (PDF) letter on the Gov.UK Notify API —
 * the call {@code NotificationClient.sendPrecompiledLetter*} makes ({@code reference}, base64 {@code content},
 * optional {@code postage}).
 *
 * <p>As for email, the request is checked the way real Gov.UK Notify checks it, so the same request behaves
 * the same with or without the simulator:
 * <ol>
 *   <li>the {@code Authorization: Bearer} contract ({@link GovUkNotifyContract}) — {@code 403 AuthError};</li>
 *   <li>a missing {@code reference} or {@code content}, content that is not a base64 PDF, or a postage other
 *       than {@code first}, {@code second} or {@code economy} — {@code 400 ValidationError};</li>
 *   <li>otherwise {@code 201} with the minted id, the reference and the postage ({@code second} when none
 *       was asked for).</li>
 * </ol>
 *
 * <p>The status polls then follow a precompiled letter's real lifecycle, one status per poll:
 * {@code pending-virus-check}, {@code accepted}, {@code received}. Real Gov.UK Notify decides a letter's
 * outcome from the PDF itself, so the simulator reads it from the PDF too: text
 * {@code simulator.notify:<status>} in the document ends the lifecycle at that status instead —
 * {@code validation-failed}, {@code virus-scan-failed}, {@code technical-failure} or
 * {@code permanent-failure} after {@code pending-virus-check}, or {@code accepted} for a letter that is
 * never received. That marker is the simulator's only lever and has no meaning to the real service.
 *
 * <p>Template (non-precompiled) letters are not simulated: a request without {@code content} is refused as
 * real Gov.UK Notify refuses a precompiled letter without it.
 */
public class GovUkNotifySendPrecompiledLetterResponseTransformer extends ResponseDefinitionTransformer {

    public static final String GOV_UK_NOTIFY_SEND_PRECOMPILED_LETTER_RESPONSE_TRANSFORMER =
            "gov-uk-notify-send-precompiled-letter-response-transformer";

    private static final Logger LOGGER = LoggerFactory.getLogger(GovUkNotifySendPrecompiledLetterResponseTransformer.class);
    private static final String REFERENCE = "reference";
    private static final String CONTENT = "content";
    private static final String POSTAGE = "postage";
    private static final String DEFAULT_POSTAGE = "second";
    private static final List<String> POSTAGES = asList("first", "second", "economy");
    private static final String PDF_HEADER = "%PDF";

    private static final String PENDING_VIRUS_CHECK = "pending-virus-check";
    private static final String ACCEPTED = "accepted";
    private static final String RECEIVED = "received";
    private static final List<String> FAILURES =
            asList("validation-failed", "virus-scan-failed", "technical-failure", "permanent-failure");
    private static final Pattern OUTCOME_MARKER = Pattern.compile("simulator\\.notify:([a-z-]+)");

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {
        final ResponseDefinition authError = GovUkNotifyContract.authError(request);
        if (authError != null) {
            LOGGER.info("Gov.UK Notify simulator: rejecting letter with 403 (missing/invalid Authorization Bearer token)");
            return authError;
        }

        final JsonObject body = GovUkNotifyContract.requestBody(request);
        final String reference = stringOrEmpty(body, REFERENCE);
        if (reference.isEmpty()) {
            return validationError("reference is a required property");
        }
        if (stringOrEmpty(body, CONTENT).isEmpty()) {
            return validationError("content is a required property");
        }
        final byte[] pdf = decodePdf(body.getString(CONTENT));
        if (pdf.length == 0) {
            return validationError("Letter content is not a valid PDF");
        }
        final String postage = body.containsKey(POSTAGE) ? stringOrEmpty(body, POSTAGE) : DEFAULT_POSTAGE;
        if (!POSTAGES.contains(postage)) {
            return validationError("postage invalid. It must be either first, second or economy.");
        }

        final List<String> lifecycle = lifecycleFor(pdf);
        final String notificationId = randomUUID().toString();
        GovUkNotifyStore.saveLetter(notificationId, postage, lifecycle);
        LOGGER.info("Gov.UK Notify simulator: accepted precompiled letter '{}' as id '{}' ({} postage, polls report {})",
                reference, notificationId, postage, lifecycle);

        return created(notificationId, reference, postage);
    }

    private static List<String> lifecycleFor(final byte[] pdf) {
        final Matcher marker = OUTCOME_MARKER.matcher(new String(pdf, StandardCharsets.ISO_8859_1));
        final String outcome = marker.find() ? marker.group(1) : RECEIVED;
        final List<String> lifecycle;
        if (FAILURES.contains(outcome)) {
            lifecycle = asList(PENDING_VIRUS_CHECK, outcome);
        } else if (ACCEPTED.equals(outcome)) {
            lifecycle = asList(PENDING_VIRUS_CHECK, ACCEPTED);
        } else {
            lifecycle = asList(PENDING_VIRUS_CHECK, ACCEPTED, RECEIVED);
        }
        return lifecycle;
    }

    private static byte[] decodePdf(final String content) {
        try {
            final byte[] decoded = Base64.getMimeDecoder().decode(content);
            final boolean isPdf = decoded.length > PDF_HEADER.length()
                    && new String(decoded, 0, PDF_HEADER.length(), StandardCharsets.ISO_8859_1).equals(PDF_HEADER);
            return isPdf ? decoded : new byte[0];
        } catch (final IllegalArgumentException e) {
            return new byte[0];
        }
    }

    private static String stringOrEmpty(final JsonObject body, final String key) {
        final JsonValue value = body.get(key);
        return value != null && value.getValueType() == JsonValue.ValueType.STRING
                ? body.getString(key).trim()
                : "";
    }

    private static ResponseDefinition created(final String notificationId, final String reference, final String postage) {
        final String responseBody = createObjectBuilder()
                .add("id", notificationId)
                .add(REFERENCE, reference)
                .add(POSTAGE, postage)
                .build().toString();

        return new ResponseDefinitionBuilder()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)
                .build();
    }

    private static ResponseDefinition validationError(final String message) {
        LOGGER.info("Gov.UK Notify simulator: rejecting letter with 400 ValidationError ({})", message);
        return GovUkNotifyContract.errorResponse(400, "ValidationError", message);
    }

    @Override
    public String getName() {
        return GOV_UK_NOTIFY_SEND_PRECOMPILED_LETTER_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }
}
