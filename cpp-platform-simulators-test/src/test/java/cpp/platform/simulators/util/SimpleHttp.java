package cpp.platform.simulators.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Minimal HTTP client for simulator transformer tests — issues a real request against the local
 * WireMock server and captures the status + body (from the error stream for 4xx/5xx). Shared so the
 * per-endpoint test classes do not each re-implement it.
 */
public final class SimpleHttp {

    private SimpleHttp() {
    }

    public static Response post(final String url, final String body, final String authorization) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        applyAuthorization(connection, authorization);
        try (OutputStream out = connection.getOutputStream()) {
            out.write(body.getBytes(UTF_8));
        }
        return read(connection);
    }

    public static Response get(final String url, final String authorization) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        applyAuthorization(connection, authorization);
        return read(connection);
    }

    private static void applyAuthorization(final HttpURLConnection connection, final String authorization) {
        if (authorization != null) {
            connection.setRequestProperty("Authorization", authorization);
        }
    }

    private static Response read(final HttpURLConnection connection) throws IOException {
        final int status = connection.getResponseCode();
        final InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        if (stream != null) {
            final byte[] chunk = new byte[1024];
            int count;
            while ((count = stream.read(chunk)) != -1) {
                buffer.write(chunk, 0, count);
            }
            stream.close();
        }
        return new Response(status, new String(buffer.toByteArray(), UTF_8));
    }

    public static final class Response {
        private final int status;
        private final String body;

        private Response(final int status, final String body) {
            this.status = status;
            this.body = body;
        }

        public int status() {
            return status;
        }

        public String body() {
            return body;
        }
    }
}
