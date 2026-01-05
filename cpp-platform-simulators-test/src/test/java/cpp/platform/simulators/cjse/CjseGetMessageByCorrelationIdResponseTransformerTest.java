package cpp.platform.simulators.cjse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import uk.gov.cjse.schemas.endpoint.types.SubmitRequest;
import uk.gov.cjse.schemas.endpoint.types.SubmitResponse;
import uk.gov.cjse.schemas.endpoint.wsdl.CJSEPort;
import uk.gov.justice.cjse.transformer.CjseGetMessageByCorrelationIdResponseTransformer;
import uk.gov.justice.cjse.transformer.CjseMessageReceivedResponseTransformer;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import cpp.platform.simulators.cjse.util.CjseSimulatorTestUtil;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CjseGetMessageByCorrelationIdResponseTransformerTest {

    private CjseSimulatorTestUtil cjseSimulatorTestUtil;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .port(8888)
            .extensions(CjseMessageReceivedResponseTransformer.class, CjseGetMessageByCorrelationIdResponseTransformer.class));

    @Before
    public void setupTest() throws IOException {

        cjseSimulatorTestUtil = new CjseSimulatorTestUtil();
        cjseSimulatorTestUtil.deleteFiles();
        wireMockRule.stubFor(post(urlPathEqualTo("/CJSE/message"))
                .willReturn(aResponse()
                        .withTransformer(CjseMessageReceivedResponseTransformer.CJSE_MESSAGE_RECEIVED_RESPONSE_TRANSFORMER, null, null)
                ));

        wireMockRule.stubFor(get(urlMatching("/newcase\\?correlation_id=(.*)$"))
                .willReturn(aResponse()
                        .withTransformer(CjseGetMessageByCorrelationIdResponseTransformer.CJSE_GET_MESSAGE_BY_CORRELATION_ID_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @After
    public void tearDown() throws IOException {
        cjseSimulatorTestUtil.deleteFiles();
    }

    @Test
    public void givenValidCorrelationIdInAnUnescapedXmlStringTheSimulatorShouldReturnTheFileContainingIt() throws DatatypeConfigurationException, MalformedURLException {

        createUnescapedXmlMessage();
        final RestClient restClient = new RestClient();
        String endpointAddress = getAbsoluteUrl("REQLUEST_AID");
        Response response = restClient.query(endpointAddress, "text/xml", new MultivaluedHashMap<>());
        Assert.assertNotNull(response);
        final String entity = response.readEntity(String.class);
        Assert.assertNotNull(entity);
        String unescapeXml = StringEscapeUtils.unescapeXml(entity);
        unescapeXml = StringEscapeUtils.unescapeXml(unescapeXml);
        Assert.assertTrue(unescapeXml.contains("<CorrelationID>REQLUEST_AID</CorrelationID>"));

    }

    @Test
    public void givenValidCorrelationIdInAnEscapedXmlStringTheSimulatorShouldReturnTheFileContainingIt() throws DatatypeConfigurationException, MalformedURLException {

        createEscapedXmlMessage();
        final RestClient restClient = new RestClient();
        String endpointAddress = getAbsoluteUrl("REQLUEST_AID");
        Response response = restClient.query(endpointAddress, "text/xml", new MultivaluedHashMap<>());
        Assert.assertNotNull(response);
        final String entity = response.readEntity(String.class);
        Assert.assertNotNull(entity);
        String unescapeXml = StringEscapeUtils.unescapeXml(entity);
        unescapeXml = StringEscapeUtils.unescapeXml(unescapeXml);
        Assert.assertTrue(unescapeXml.contains("<CorrelationID>REQLUEST_AID</CorrelationID>"));

    }

    private void createEscapedXmlMessage() throws DatatypeConfigurationException, MalformedURLException {

        CJSEPort cjsePort = cjseSimulatorTestUtil.getWebServicePort();
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("REQUST_ID", TestData.XML_MESSAGE_ESCAPED);
        final SubmitResponse submitResponse = cjsePort.submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }

    private String getAbsoluteUrl(final String correlationId) {
        return cjseSimulatorTestUtil.getBaseUri() + "/newcase?correlation_id=" + correlationId;
    }


    private void createUnescapedXmlMessage() throws DatatypeConfigurationException, MalformedURLException {

        CJSEPort cjsePort = cjseSimulatorTestUtil.getWebServicePort();
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("2e372980-0c17-4f20-8591-c84e797fa317", TestData.XML_MESSAGE_UNESCAPED);
        final SubmitResponse submitResponse = cjsePort.submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }

}
