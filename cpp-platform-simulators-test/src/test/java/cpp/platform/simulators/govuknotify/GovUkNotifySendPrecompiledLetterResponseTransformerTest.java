package cpp.platform.simulators.govuknotify;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static cpp.platform.simulators.util.FileUtil.deleteFiles;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.govuknotify.GovUkNotifyGetStatusResponseTransformer.GOV_UK_NOTIFY_GET_STATUS_RESPONSE_TRANSFORMER;
import static uk.gov.justice.govuknotify.GovUkNotifySendPrecompiledLetterResponseTransformer.GOV_UK_NOTIFY_SEND_PRECOMPILED_LETTER_RESPONSE_TRANSFORMER;

import cpp.platform.simulators.util.SimpleHttp;
import cpp.platform.simulators.util.SimpleHttp.Response;
import uk.gov.justice.govuknotify.GovUkNotifyGetStatusResponseTransformer;
import uk.gov.justice.govuknotify.GovUkNotifySendPrecompiledLetterResponseTransformer;
import uk.gov.justice.govuknotify.GovUkNotifyStore;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GovUkNotifySendPrecompiledLetterResponseTransformerTest {

    private static final int PORT = 8899;
    private static final String BASE = "http://localhost:" + PORT;
    private static final String LETTER_PATH = "/v2/notifications/letter";
    private static final String STATUS_PATH_REGEX = "/v2/notifications/[0-9a-fA-F-]{36}";
    private static final String NOTIFICATIONS_PATH = "/v2/notifications/";
    private static final String BEARER = "Bearer dummy.jwt.token";
    private static final String STATUS = "status";
    private static final String POSTAGE = "postage";
    private static final String ORDINARY_LETTER = "Dear recipient";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT).extensions(
            GovUkNotifySendPrecompiledLetterResponseTransformer.class,
            GovUkNotifyGetStatusResponseTransformer.class));

    @Before
    public void setUp() throws IOException {
        deleteFiles(GovUkNotifyStore.NOTIFY_DIR);
        wireMockRule.stubFor(post(urlPathEqualTo(LETTER_PATH))
                .willReturn(aResponse().withTransformer(GOV_UK_NOTIFY_SEND_PRECOMPILED_LETTER_RESPONSE_TRANSFORMER, null, null)));
        wireMockRule.stubFor(get(urlPathMatching(STATUS_PATH_REGEX))
                .willReturn(aResponse().withTransformer(GOV_UK_NOTIFY_GET_STATUS_RESPONSE_TRANSFORMER, null, null)));
    }

    @After
    public void tearDown() throws IOException {
        deleteFiles(GovUkNotifyStore.NOTIFY_DIR);
    }

    @Test
    public void shouldAcceptALetterAsSecondClassAndTakeItThroughToReceived() throws IOException {
        final String reference = UUID.randomUUID().toString();
        final Response send = SimpleHttp.post(BASE + LETTER_PATH, letterBody(reference, pdf(ORDINARY_LETTER), null), BEARER);

        assertThat(send.status(), is(201));
        final JsonObject created = json(send.body());
        assertThat(created.getString("id"), notNullValue());
        assertThat(created.getString("reference"), is(reference));
        assertThat(created.getString(POSTAGE), is("second"));

        final String notificationId = created.getString("id");
        final JsonObject firstPoll = poll(notificationId);
        assertThat(firstPoll.getString("type"), is("letter"));
        assertThat(firstPoll.getString(POSTAGE), is("second"));
        assertThat(firstPoll.getString(STATUS), is("pending-virus-check"));
        assertThat(poll(notificationId).getString(STATUS), is("accepted"));
        assertThat(poll(notificationId).getString(STATUS), is("received"));
        assertThat(poll(notificationId).getString(STATUS), is("received"));
    }

    @Test
    public void shouldAcceptAFirstClassLetterAndReportItsPostage() throws IOException {
        final Response send = SimpleHttp.post(BASE + LETTER_PATH,
                letterBody(UUID.randomUUID().toString(), pdf(ORDINARY_LETTER), "first"), BEARER);

        assertThat(send.status(), is(201));
        assertThat(json(send.body()).getString(POSTAGE), is("first"));
        assertThat(poll(json(send.body()).getString("id")).getString(POSTAGE), is("first"));
    }

    @Test
    public void shouldEndTheLifecycleAtTheFailureNamedInTheLetter() throws IOException {
        final Response send = SimpleHttp.post(BASE + LETTER_PATH,
                letterBody(UUID.randomUUID().toString(), pdf("simulator.notify:validation-failed"), null), BEARER);
        final String notificationId = json(send.body()).getString("id");

        assertThat(poll(notificationId).getString(STATUS), is("pending-virus-check"));
        assertThat(poll(notificationId).getString(STATUS), is("validation-failed"));
        assertThat(poll(notificationId).getString(STATUS), is("validation-failed"));
    }

    @Test
    public void shouldKeepALetterMarkedAcceptedAtAccepted() throws IOException {
        final Response send = SimpleHttp.post(BASE + LETTER_PATH,
                letterBody(UUID.randomUUID().toString(), pdf("simulator.notify:accepted"), null), BEARER);
        final String notificationId = json(send.body()).getString("id");

        assertThat(poll(notificationId).getString(STATUS), is("pending-virus-check"));
        assertThat(poll(notificationId).getString(STATUS), is("accepted"));
        assertThat(poll(notificationId).getString(STATUS), is("accepted"));
    }

    @Test
    public void shouldEndTheLifecycleAtPermanentFailureNamedInTheLetter() throws IOException {
        assertThat(finalStatusOfALetterMarked("permanent-failure"), is("permanent-failure"));
    }

    @Test
    public void shouldEndTheLifecycleAtTechnicalFailureNamedInTheLetter() throws IOException {
        assertThat(finalStatusOfALetterMarked("technical-failure"), is("technical-failure"));
    }

    @Test
    public void shouldEndTheLifecycleAtVirusScanFailedNamedInTheLetter() throws IOException {
        assertThat(finalStatusOfALetterMarked("virus-scan-failed"), is("virus-scan-failed"));
    }

    @Test
    public void shouldAcceptEconomyPostage() throws IOException {
        final Response send = SimpleHttp.post(BASE + LETTER_PATH,
                letterBody(UUID.randomUUID().toString(), pdf(ORDINARY_LETTER), "economy"), BEARER);

        assertThat(send.status(), is(201));
        assertThat(json(send.body()).getString(POSTAGE), is("economy"));
    }

    @Test
    public void shouldReportSecondClassForALetterRecordedWithoutPostage() throws IOException {
        final String notificationId = UUID.randomUUID().toString();
        Files.createDirectories(Paths.get(GovUkNotifyStore.NOTIFY_DIR));
        Files.write(Paths.get(GovUkNotifyStore.NOTIFY_DIR, notificationId),
                "letter\nreceived".getBytes(StandardCharsets.UTF_8));

        final JsonObject polled = poll(notificationId);
        assertThat(polled.getString("type"), is("letter"));
        assertThat(polled.getString(STATUS), is("received"));
        assertThat(polled.getString(POSTAGE), is("second"));
    }

    @Test
    public void shouldRejectALetterWithoutContentWith400() throws IOException {
        final String body = createObjectBuilder().add("reference", UUID.randomUUID().toString()).build().toString();
        final Response send = SimpleHttp.post(BASE + LETTER_PATH, body, BEARER);

        assertThat(send.status(), is(400));
        assertThat(validationMessage(send), is("content is a required property"));
    }

    @Test
    public void shouldRejectALetterWithoutAReferenceWith400() throws IOException {
        final Response send = SimpleHttp.post(BASE + LETTER_PATH, letterBody(null, pdf(ORDINARY_LETTER), null), BEARER);

        assertThat(send.status(), is(400));
        assertThat(validationMessage(send), is("reference is a required property"));
    }

    @Test
    public void shouldRejectContentThatIsNotAPdfWith400() throws IOException {
        final String notAPdf = Base64.getEncoder().encodeToString("plain text".getBytes(ISO_8859_1));
        final Response send = SimpleHttp.post(BASE + LETTER_PATH, letterBody(UUID.randomUUID().toString(), notAPdf, null), BEARER);

        assertThat(send.status(), is(400));
        assertThat(validationMessage(send), is("Letter content is not a valid PDF"));
    }

    @Test
    public void shouldRejectAnUnknownPostageWith400() throws IOException {
        final Response send = SimpleHttp.post(BASE + LETTER_PATH,
                letterBody(UUID.randomUUID().toString(), pdf(ORDINARY_LETTER), "third"), BEARER);

        assertThat(send.status(), is(400));
        assertThat(validationMessage(send), is("postage invalid. It must be either first, second or economy."));
    }

    @Test
    public void shouldRejectALetterWithNoAuthorizationHeaderWith403() throws IOException {
        final Response send = SimpleHttp.post(BASE + LETTER_PATH,
                letterBody(UUID.randomUUID().toString(), pdf(ORDINARY_LETTER), null), null);

        assertThat(send.status(), is(403));
        assertThat(json(send.body()).getJsonArray("errors").getJsonObject(0).getString("error"), is("AuthError"));
    }

    private static String finalStatusOfALetterMarked(final String outcome) throws IOException {
        final Response send = SimpleHttp.post(BASE + LETTER_PATH,
                letterBody(UUID.randomUUID().toString(), pdf("simulator.notify:" + outcome), null), BEARER);
        final String notificationId = json(send.body()).getString("id");
        assertThat(poll(notificationId).getString(STATUS), is("pending-virus-check"));
        return poll(notificationId).getString(STATUS);
    }

    private static String pdf(final String text) {
        final String document = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog >>\nendobj\nBT (" + text + ") Tj ET\n%%EOF\n";
        return Base64.getEncoder().encodeToString(document.getBytes(ISO_8859_1));
    }

    private static String letterBody(final String reference, final String content, final String postage) {
        final JsonObjectBuilder body = createObjectBuilder().add("content", content);
        if (reference != null) {
            body.add("reference", reference);
        }
        if (postage != null) {
            body.add(POSTAGE, postage);
        }
        return body.build().toString();
    }

    private static JsonObject poll(final String notificationId) throws IOException {
        final Response poll = SimpleHttp.get(BASE + NOTIFICATIONS_PATH + notificationId, BEARER);
        assertThat(poll.status(), is(200));
        return json(poll.body());
    }

    private static String validationMessage(final Response response) {
        final JsonObject error = json(response.body()).getJsonArray("errors").getJsonObject(0);
        assertThat(error.getString("error"), is("ValidationError"));
        return error.getString("message");
    }

    private static JsonObject json(final String body) {
        return createReader(new StringReader(body)).readObject();
    }
}
