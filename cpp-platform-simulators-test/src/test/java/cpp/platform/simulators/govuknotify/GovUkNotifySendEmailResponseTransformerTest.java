package cpp.platform.simulators.govuknotify;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static cpp.platform.simulators.util.FileUtil.deleteFiles;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.govuknotify.GovUkNotifyGetStatusResponseTransformer.GOV_UK_NOTIFY_GET_STATUS_RESPONSE_TRANSFORMER;
import static uk.gov.justice.govuknotify.GovUkNotifySendEmailResponseTransformer.GOV_UK_NOTIFY_SEND_EMAIL_RESPONSE_TRANSFORMER;

import cpp.platform.simulators.util.SimpleHttp;
import cpp.platform.simulators.util.SimpleHttp.Response;
import uk.gov.justice.govuknotify.GovUkNotifyGetStatusResponseTransformer;
import uk.gov.justice.govuknotify.GovUkNotifySendEmailResponseTransformer;
import uk.gov.justice.govuknotify.GovUkNotifyStore;

import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GovUkNotifySendEmailResponseTransformerTest {

    private static final int PORT = 8899;
    private static final String BASE = "http://localhost:" + PORT;
    private static final String EMAIL_PATH = "/v2/notifications/email";
    private static final String STATUS_PATH_REGEX = "/v2/notifications/[0-9a-fA-F-]{36}";
    private static final String BEARER = "Bearer dummy.jwt.token";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT).extensions(
            GovUkNotifySendEmailResponseTransformer.class,
            GovUkNotifyGetStatusResponseTransformer.class));

    @Before
    public void setUp() throws IOException {
        deleteFiles(GovUkNotifyStore.NOTIFY_DIR);
        wireMockRule.stubFor(post(urlPathEqualTo(EMAIL_PATH))
                .willReturn(aResponse().withTransformer(GOV_UK_NOTIFY_SEND_EMAIL_RESPONSE_TRANSFORMER, null, null)));
        wireMockRule.stubFor(get(urlPathMatching(STATUS_PATH_REGEX))
                .willReturn(aResponse().withTransformer(GOV_UK_NOTIFY_GET_STATUS_RESPONSE_TRANSFORMER, null, null)));
    }

    @After
    public void tearDown() throws IOException {
        deleteFiles(GovUkNotifyStore.NOTIFY_DIR);
    }

    @Test
    public void shouldAcceptSendAndReportDeliveredForOrdinaryRecipient() throws IOException {
        final Response send = SimpleHttp.post(BASE + EMAIL_PATH, sendBody("someone@example.com", MaterialUrl.STRING), BEARER);

        assertThat(send.status(), is(201));
        final String notificationId = json(send.body()).getString("id");
        assertThat(notificationId, notNullValue());

        final Response poll = SimpleHttp.get(BASE + "/v2/notifications/" + notificationId, BEARER);
        assertThat(poll.status(), is(200));
        assertThat(json(poll.body()).getString("status"), is("delivered"));
    }

    @Test
    public void shouldAcceptSendWhenMaterialUrlIsAnAttachmentObject() throws IOException {
        final Response send = SimpleHttp.post(BASE + EMAIL_PATH, sendBody("someone@example.com", MaterialUrl.OBJECT), BEARER);

        assertThat(send.status(), is(201));
    }

    @Test
    public void shouldReportPermanentFailureForReservedPermFailAddress() throws IOException {
        final Response send = SimpleHttp.post(BASE + EMAIL_PATH, sendBody("perm-fail@simulator.notify", MaterialUrl.STRING), BEARER);
        final String notificationId = json(send.body()).getString("id");

        final Response poll = SimpleHttp.get(BASE + "/v2/notifications/" + notificationId, BEARER);
        assertThat(json(poll.body()).getString("status"), is("permanent-failure"));
    }

    @Test
    public void shouldReportTemporaryFailureForReservedTempFailAddress() throws IOException {
        final Response send = SimpleHttp.post(BASE + EMAIL_PATH, sendBody("temp-fail@simulator.notify", MaterialUrl.STRING), BEARER);
        final String notificationId = json(send.body()).getString("id");

        final Response poll = SimpleHttp.get(BASE + "/v2/notifications/" + notificationId, BEARER);
        assertThat(json(poll.body()).getString("status"), is("temporary-failure"));
    }

    @Test
    public void shouldRejectSendMissingMaterialUrlPersonalisationWith400() throws IOException {
        final Response send = SimpleHttp.post(BASE + EMAIL_PATH, sendBody("someone@example.com", MaterialUrl.NONE), BEARER);

        assertThat(send.status(), is(400));
        assertThat(json(send.body()).getInt("status_code"), is(400));
        assertThat(json(send.body()).getJsonArray("errors").getJsonObject(0).getString("error"), is("BadRequestError"));
    }

    @Test
    public void shouldRejectSendWithNoPersonalisationBlockWith400() throws IOException {
        final String body = createObjectBuilder()
                .add("template_id", UUID.randomUUID().toString())
                .add("email_address", "someone@example.com")
                .build().toString();

        final Response send = SimpleHttp.post(BASE + EMAIL_PATH, body, BEARER);

        assertThat(send.status(), is(400));
    }

    @Test
    public void shouldRejectSendWithNoAuthorizationHeaderWith403() throws IOException {
        final Response send = SimpleHttp.post(BASE + EMAIL_PATH, sendBody("someone@example.com", MaterialUrl.STRING), null);

        assertThat(send.status(), is(403));
        assertThat(json(send.body()).getJsonArray("errors").getJsonObject(0).getString("error"), is("AuthError"));
    }

    @Test
    public void shouldRejectSendWithNonBearerAuthorizationWith403() throws IOException {
        final Response send = SimpleHttp.post(BASE + EMAIL_PATH, sendBody("someone@example.com", MaterialUrl.STRING), "Basic dXNlcjpwYXNz");

        assertThat(send.status(), is(403));
    }

    private enum MaterialUrl { STRING, OBJECT, NONE }

    private static String sendBody(final String emailAddress, final MaterialUrl materialUrl) {
        final JsonObjectBuilder personalisation = createObjectBuilder();
        if (materialUrl == MaterialUrl.STRING) {
            personalisation.add("material_url", "https://example.com/document.pdf");
        } else if (materialUrl == MaterialUrl.OBJECT) {
            personalisation.add("material_url", createObjectBuilder()
                    .add("file", "SGVsbG8=")
                    .add("filename", "document.pdf"));
        }
        return createObjectBuilder()
                .add("template_id", UUID.randomUUID().toString())
                .add("email_address", emailAddress)
                .add("reference", UUID.randomUUID().toString())
                .add("personalisation", personalisation)
                .build().toString();
    }

    private static JsonObject json(final String body) {
        return createReader(new StringReader(body)).readObject();
    }
}
