package cpp.platform.simulators.govuknotify;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static cpp.platform.simulators.util.FileUtil.deleteFiles;
import static javax.json.Json.createReader;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.govuknotify.GovUkNotifyGetStatusResponseTransformer.GOV_UK_NOTIFY_GET_STATUS_RESPONSE_TRANSFORMER;

import cpp.platform.simulators.util.SimpleHttp;
import cpp.platform.simulators.util.SimpleHttp.Response;
import uk.gov.justice.govuknotify.GovUkNotifyGetStatusResponseTransformer;
import uk.gov.justice.govuknotify.GovUkNotifyStore;

import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

import javax.json.JsonObject;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GovUkNotifyGetStatusResponseTransformerTest {

    private static final int PORT = 8899;
    private static final String BASE = "http://localhost:" + PORT;
    private static final String STATUS_PATH_REGEX = "/v2/notifications/[0-9a-fA-F-]{36}";
    private static final String BEARER = "Bearer dummy.jwt.token";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT).extensions(
            GovUkNotifyGetStatusResponseTransformer.class));

    @Before
    public void setUp() throws IOException {
        deleteFiles(GovUkNotifyStore.NOTIFY_DIR);
        wireMockRule.stubFor(get(urlPathMatching(STATUS_PATH_REGEX))
                .willReturn(aResponse().withTransformer(GOV_UK_NOTIFY_GET_STATUS_RESPONSE_TRANSFORMER, null, null)));
    }

    @After
    public void tearDown() throws IOException {
        deleteFiles(GovUkNotifyStore.NOTIFY_DIR);
    }

    @Test
    public void shouldDefaultToDeliveredForAnUnknownNotificationId() throws IOException {
        final Response poll = SimpleHttp.get(BASE + "/v2/notifications/" + UUID.randomUUID(), BEARER);

        assertThat(poll.status(), is(200));
        final JsonObject body = json(poll.body());
        assertThat(body.getString("status"), is("delivered"));
        assertThat(body.getString("type"), is("email"));
    }

    @Test
    public void shouldRejectStatusPollWithNoAuthorizationHeaderWith403() throws IOException {
        final Response poll = SimpleHttp.get(BASE + "/v2/notifications/" + UUID.randomUUID(), null);

        assertThat(poll.status(), is(403));
        assertThat(json(poll.body()).getJsonArray("errors").getJsonObject(0).getString("error"), is("AuthError"));
    }

    private static JsonObject json(final String body) {
        return createReader(new StringReader(body)).readObject();
    }
}
