package cpp.platform.simulators.dart;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import uk.gov.justice.darts.transformer.DartsServiceResponseTransformer;
import uk.gov.justice.services.test.utils.core.rest.RestClient;
import java.io.IOException;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.darts.transformer.DartsServiceResponseTransformer.DARTS_SERVICE_RESPONSE_TRANSFORMER;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.service.mojdarts.synapps.com.DARTSService;
import com.service.mojdarts.synapps.com.DARTSServicePort;
import com.synapps.moj.dfs.response.DARTSResponse;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DartsServiceResponseTransformerTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .port(8888)
            .extensions(DartsServiceResponseTransformer.class));

    @Before
    public void setupTest() throws IOException {

        wireMockRule.stubFor(post(urlPathEqualTo("/DARTS"))
                .willReturn(aResponse()
                        .withTransformer(DARTS_SERVICE_RESPONSE_TRANSFORMER, null, null)
                ));
        wireMockRule.stubFor(get(urlPathEqualTo("/DARTS_WSDL"))
                .willReturn(aResponse()
                        .withTransformer(DARTS_SERVICE_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @Test
    public void shouldAddDocument() throws com.service.mojdarts.synapps.com.ServiceException {
        DARTSServicePort dartsService = new DARTSService().getDARTSServicePort();
        DARTSResponse dartsResponse = dartsService.addDocument("test", "test", "test", "test");
        assertNotNull(dartsResponse);
        assertEquals("200", dartsResponse.getCode());
        assertEquals("OK", dartsResponse.getMessage());
    }

    @Test
    public void shouldReturnWsdl() {
        final RestClient restClient = new RestClient();
        String endpointAddress = "http://localhost:8888/DARTS_WSDL";
        Response response = restClient.query(endpointAddress, "text/xml", new MultivaluedHashMap<>());
        assertNotNull(response);
        assertThat(response.readEntity(String.class), Matchers.containsString("http://com.synapps.mojdarts.service.com"));
    }
}
