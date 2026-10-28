package uk.gov.justice.govuknotify;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Correlates the calls the Gov.UK Notify client makes: the send POST mints a notification id and
 * records the statuses the later status polls should report; each status GET replays the next one.
 *
 * <p>This mirrors the write-on-POST / read-on-GET file idiom already used by the CPS and CJSE
 * simulators (see {@code ApplicationNotificationSentToCpsResponseTransformer} /
 * {@code CjseGetMessageByCorrelationIdResponseTransformer}) — WireMock is otherwise stateless, so
 * without this the status GET could not know what the send POST decided.
 *
 * <p>An email records a single status. A letter records the lifecycle real Gov.UK Notify takes it
 * through (e.g. {@code pending-virus-check}, {@code accepted}, {@code received}): each poll reports
 * the next status and the last one is then reported on every later poll, so a caller that polls
 * until a final status sees the same progression as against the real service.
 *
 * <p>File layout, one line each: type ({@code email} / {@code letter}), the statuses still to report
 * (comma-separated), and for a letter its postage.
 */
public final class GovUkNotifyStore {

    /** Under the JVM temp dir (not a hardcoded public path) — per-pod, transient correlation only. */
    public static final String NOTIFY_DIR =
            Paths.get(System.getProperty("java.io.tmpdir"), "NotifyMessages").toString();
    public static final String TYPE_EMAIL = "email";
    public static final String TYPE_LETTER = "letter";
    private static final String DEFAULT_STATUS = "delivered";
    private static final String DEFAULT_POSTAGE = "second";
    private static final String STATUS_SEPARATOR = ",";
    private static final String LINE_SEPARATOR = "\n";
    private static final Object LOCK = new Object();
    private static final Logger LOGGER = LoggerFactory.getLogger(GovUkNotifyStore.class);

    private GovUkNotifyStore() {
    }

    public static void saveStatus(final String notificationId, final String status) {
        write(notificationId, asList(TYPE_EMAIL, status));
    }

    public static void saveLetter(final String notificationId, final String postage, final List<String> lifecycle) {
        write(notificationId, asList(TYPE_LETTER, String.join(STATUS_SEPARATOR, lifecycle), postage));
    }

    /**
     * @return the next status recorded by the send POST for this id, moving the lifecycle on by one
     * (the last status is kept); an email {@code delivered} when nothing was recorded (e.g. a poll for an
     * id this simulator never issued) so a stray poll degrades gracefully.
     */
    public static Polled poll(final String notificationId) {
        synchronized (LOCK) {
            final Path path = Paths.get(NOTIFY_DIR, notificationId);
            if (!Files.exists(path)) {
                return new Polled(TYPE_EMAIL, DEFAULT_STATUS, DEFAULT_POSTAGE);
            }
            try {
                final List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                final String type = lines.get(0).trim();
                final List<String> statuses = new ArrayList<>(asList(lines.get(1).trim().split(STATUS_SEPARATOR)));
                final String postage = lines.size() > 2 && !lines.get(2).trim().isEmpty()
                        ? lines.get(2).trim()
                        : DEFAULT_POSTAGE;
                final String status = statuses.get(0);
                if (statuses.size() > 1) {
                    lines.set(1, String.join(STATUS_SEPARATOR, statuses.subList(1, statuses.size())));
                    Files.write(path, toBytes(lines));
                }
                return new Polled(type, status, postage);
            } catch (final IOException | IndexOutOfBoundsException e) {
                LOGGER.warn("Could not read Gov.UK Notify status for '{}', defaulting to '{}'", notificationId, DEFAULT_STATUS, e);
                return new Polled(TYPE_EMAIL, DEFAULT_STATUS, DEFAULT_POSTAGE);
            }
        }
    }

    private static void write(final String notificationId, final List<String> lines) {
        synchronized (LOCK) {
            try {
                Files.createDirectories(Paths.get(NOTIFY_DIR));
                Files.write(Paths.get(NOTIFY_DIR, notificationId), toBytes(lines));
            } catch (final IOException e) {
                LOGGER.warn("Could not persist Gov.UK Notify status for '{}'", notificationId, e);
            }
        }
    }

    private static byte[] toBytes(final List<String> lines) {
        return String.join(LINE_SEPARATOR, lines).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * What a status poll reports: the notification's type, its status now and its postage ({@code second} when
     * none was recorded, which is Gov.UK Notify's default; only a letter reports it).
     */
    public static final class Polled {
        private final String type;
        private final String status;
        private final String postage;

        Polled(final String type, final String status, final String postage) {
            this.type = type;
            this.status = status;
            this.postage = postage;
        }

        public String getType() {
            return type;
        }

        public String getStatus() {
            return status;
        }

        public String getPostage() {
            return postage;
        }

        public boolean isLetter() {
            return TYPE_LETTER.equals(type);
        }
    }
}
