package cpp.platform.simulators.dart;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotNull;
import static uk.gov.justice.darts.transformer.DartsServiceContextRegistryResponseTransformer.DARTS_SERVICE_CONTEXT_REGISTRY_RESPONSE_TRANSFORMER;
import uk.gov.justice.darts.transformer.DartsServiceContextRegistryResponseTransformer;
import uk.gov.justice.services.test.utils.core.rest.RestClient;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DartsServiceContextRegistryResponseTransformerTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .port(8888)
            .extensions(DartsServiceContextRegistryResponseTransformer.class));

    @Before
    public void setupTest() {

        wireMockRule.stubFor(post(urlPathEqualTo("/ContextRegistry"))
                .willReturn(aResponse()
                        .withTransformer(DARTS_SERVICE_CONTEXT_REGISTRY_RESPONSE_TRANSFORMER, null, null)
                ));
        wireMockRule.stubFor(get(urlPathEqualTo("/ContextRegistry_WSDL"))
                .willReturn(aResponse()
                        .withTransformer(DARTS_SERVICE_CONTEXT_REGISTRY_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @Test
    public void shouldRegister() {
        final RestClient restClient = new RestClient();
        String endpointAddress = "http://localhost:8888/ContextRegistry";
        Response response = restClient.postCommand(endpointAddress, "text/xml",
                "", new MultivaluedHashMap<>());
        assertNotNull(response);
        assertThat(response.readEntity(String.class),containsString("<typ:registerResponse>"));
    }

    @Test
    public void shouldReturnWsdl() {
        final RestClient restClient = new RestClient();
        String endpointAddress = "http://localhost:8888/ContextRegistry_WSDL";
        Response response = restClient.query(endpointAddress, "text/xml", new MultivaluedHashMap<>());
        assertNotNull(response);
        assertThat(response.readEntity(String.class), Matchers.containsString("http://services.rt.fs.documentum.emc.com"));
    }
}
