package uk.gov.justice.govuknotify;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.json.JsonObject;
import javax.json.JsonReader;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enforces the parts of the real Gov.UK Notify request contract that must be present for a call to be
 * accepted, so the same configuration is exercised in lower environments as in production.
 *
 * <p>The value can be a dummy key, but it must be there: real Gov.UK Notify requires an
 * {@code Authorization: Bearer <jwt>} header (the notifications-java-client builds this from the API
 * key) and returns {@code 403 AuthError} without it. Mirroring that here means a missing/blank API-key
 * configuration fails against the simulator too, rather than passing silently and only breaking once
 * the simulator is turned off in a higher environment.
 *
 * <p>Only presence and shape are checked — a non-empty Bearer token must be supplied. The JWT
 * signature is deliberately NOT verified: the simulator has no service secret and the key is a dummy.
 */
public final class GovUkNotifyContract {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Logger LOGGER = LoggerFactory.getLogger(GovUkNotifyContract.class);

    private GovUkNotifyContract() {
    }

    /**
     * @return a {@code 403 AuthError} response (mirroring real Gov.UK Notify) when the Authorization
     * header is absent or is not a non-empty Bearer token, or {@code null} when the request may proceed.
     */
    public static ResponseDefinition authError(final Request request) {
        final String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.trim().isEmpty()) {
            return forbidden("Unauthorized: authentication token must be provided");
        }
        if (!authorization.startsWith(BEARER_PREFIX)
                || authorization.substring(BEARER_PREFIX.length()).trim().isEmpty()) {
            return forbidden("Invalid token: authorization header not in correct format");
        }
        return null;
    }

    /**
     * @return a Gov.UK Notify error response — {@code {"errors":[{"error","message"}],"status_code"}}, the
     * shape every real Notify error takes — with that HTTP status.
     */
    public static ResponseDefinition errorResponse(final int status, final String error, final String message) {
        final String body = createObjectBuilder()
                .add("errors", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("error", error)
                                .add("message", message)))
                .add("status_code", status)
                .build().toString();

        return new ResponseDefinitionBuilder()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody(body)
                .build();
    }

    /**
     * @return the request's JSON body, or an empty object when there is none or it cannot be parsed, so a
     * malformed request is refused by the same field checks as one that omits the fields.
     */
    public static JsonObject requestBody(final Request request) {
        final byte[] raw = request.getBody();
        final String json = (raw == null || raw.length == 0) ? "{}" : new String(raw, StandardCharsets.UTF_8);
        try (JsonReader reader = createReader(new StringReader(json))) {
            return reader.readObject();
        } catch (final RuntimeException e) {
            LOGGER.warn("Gov.UK Notify simulator: could not parse request body, treating as empty", e);
            return createObjectBuilder().build();
        }
    }

    private static ResponseDefinition forbidden(final String message) {
        return errorResponse(403, "AuthError", message);
    }
}
