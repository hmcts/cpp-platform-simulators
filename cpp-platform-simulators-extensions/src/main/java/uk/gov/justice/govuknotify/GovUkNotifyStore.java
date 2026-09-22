package uk.gov.justice.govuknotify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Correlates the two calls the Gov.UK Notify client makes: the send POST mints a notification id and
 * records the delivery status that the later status poll should report; the status GET replays it.
 *
 * <p>This mirrors the write-on-POST / read-on-GET file idiom already used by the CPS and CJSE
 * simulators (see {@code ApplicationNotificationSentToCpsResponseTransformer} /
 * {@code CjseGetMessageByCorrelationIdResponseTransformer}) — WireMock is otherwise stateless, so
 * without this the status GET could not know what the send POST decided.
 */
public final class GovUkNotifyStore {

    /** Under the JVM temp dir (not a hardcoded public path) — per-pod, transient correlation only. */
    public static final String NOTIFY_DIR =
            Paths.get(System.getProperty("java.io.tmpdir"), "NotifyMessages").toString();
    private static final String DEFAULT_STATUS = "delivered";
    private static final Logger LOGGER = LoggerFactory.getLogger(GovUkNotifyStore.class);

    private GovUkNotifyStore() {
    }

    public static void saveStatus(final String notificationId, final String status) {
        try {
            Files.createDirectories(Paths.get(NOTIFY_DIR));
            Files.write(Paths.get(NOTIFY_DIR, notificationId), status.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException e) {
            LOGGER.warn("Could not persist Gov.UK Notify status for '{}'", notificationId, e);
        }
    }

    /**
     * @return the status recorded by the send POST, or {@code delivered} when nothing was recorded
     * (e.g. a poll for an id this simulator never issued) so a stray poll degrades gracefully.
     */
    public static String readStatus(final String notificationId) {
        final Path path = Paths.get(NOTIFY_DIR, notificationId);
        if (!Files.exists(path)) {
            return DEFAULT_STATUS;
        }
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
        } catch (final IOException e) {
            LOGGER.warn("Could not read Gov.UK Notify status for '{}', defaulting to '{}'", notificationId, DEFAULT_STATUS, e);
            return DEFAULT_STATUS;
        }
    }
}
