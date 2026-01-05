package cpp.platform.simulators.cjse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertTrue;

import uk.gov.cjse.schemas.endpoint.types.ExecMode;
import uk.gov.cjse.schemas.endpoint.types.ObjectFactory;
import uk.gov.cjse.schemas.endpoint.types.SubmitRequest;
import uk.gov.cjse.schemas.endpoint.types.SubmitResponse;
import uk.gov.cjse.schemas.endpoint.wsdl.CJSEPort;
import uk.gov.justice.cjse.transformer.CjseMessageReceivedResponseTransformer;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;

import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import cpp.platform.simulators.cjse.util.CjseSimulatorTestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CjseMessageReceivedResponseTransformerTest {

    private static final QName SERVICE_NAME
            = new QName("http://schemas.cjse.gov.uk/endpoint/wsdl/", "CJSEService");
    private static final QName PORT_NAME
            = new QName("http://schemas.cjse.gov.uk/endpoint/wsdl/", "CJSEPort");

    private CjseSimulatorTestUtil cjseSimulatorTestUtil;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888).extensions(CjseMessageReceivedResponseTransformer.class));

    @Before
    public void setupTest() throws IOException {

        cjseSimulatorTestUtil = new CjseSimulatorTestUtil();
        cjseSimulatorTestUtil.deleteFiles();

        wireMockRule.stubFor(post(urlPathEqualTo("/CJSE/message"))
                .willReturn(aResponse()
                        .withTransformer(CjseMessageReceivedResponseTransformer.CJSE_MESSAGE_RECEIVED_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @After
    public void tearDown() throws IOException {
        cjseSimulatorTestUtil.deleteFiles();
    }

    @Test
    public void givenValidsoaprequesttocjsesimulatorSuccessMessagereceived() throws DatatypeConfigurationException, MalformedURLException {

        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("REQUST_ID", TestData.XML_MESSAGE_ESCAPED);
        final SubmitResponse submitResponse = cjseSimulatorTestUtil.getWebServicePort().submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }

    @Test
    public void givenSoapRequestWithCorrelationIdError200SimulatorShouldReturnFailureMessageWith200ResponseCode() throws DatatypeConfigurationException, MalformedURLException {

        final SubmitRequest submitRequest = createSubmitRequest("ERROR_200");
        final SubmitResponse submitResponse = cjseSimulatorTestUtil.getWebServicePort().submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(200, submitResponse.getResponseCode());
        Assert.assertEquals("ERROR", submitResponse.getResponseText());

    }

    @Test
    public void givenSoapRequestWithCorrelationIdError201SimulatorShouldReturnFailureMessageWith201ResponseCode() throws DatatypeConfigurationException, MalformedURLException {

        final SubmitRequest submitRequest = createSubmitRequest("ERROR_201");
        final SubmitResponse submitResponse = cjseSimulatorTestUtil.getWebServicePort().submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(201, submitResponse.getResponseCode());
        Assert.assertEquals("TEMPORARY PROBLEM", submitResponse.getResponseText());
    }

    @Test
    public void givenSoapRequestWithCorrelationIdError202SimulatorShouldReturnFailureMessageWith202ResponseCode() throws DatatypeConfigurationException, MalformedURLException {

        final SubmitRequest submitRequest = createSubmitRequest("ERROR_202");
        final SubmitResponse submitResponse = cjseSimulatorTestUtil.getWebServicePort().submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(202, submitResponse.getResponseCode());
        Assert.assertEquals("SERVER FAILURE", submitResponse.getResponseText());
    }

    @Test
    public void givenSoapRequestWithCorrelationIdError300SimulatorShouldReturnFailureMessageWith300ResponseCode() throws DatatypeConfigurationException, MalformedURLException {

        final SubmitRequest submitRequest = createSubmitRequest("ERROR_300");
        final SubmitResponse submitResponse = cjseSimulatorTestUtil.getWebServicePort().submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(300, submitResponse.getResponseCode());
        Assert.assertEquals("FATAL ERROR", submitResponse.getResponseText());
    }

    @Test
    public void givenSoapRequestWithCorrelationIdError304SimulatorShouldReturnFailureMessageWith304ResponseCode() throws DatatypeConfigurationException, MalformedURLException {

        final SubmitRequest submitRequest = createSubmitRequest("ERROR_304");
        final SubmitResponse submitResponse = cjseSimulatorTestUtil.getWebServicePort().submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(304, submitResponse.getResponseCode());
        Assert.assertEquals("WRONG SOURCE", submitResponse.getResponseText());
    }

    @Test
    public void givenSoapRequestWithCorrelationIdError305SimulatorShouldReturnFailureMessageWith305ResponseCode() throws DatatypeConfigurationException, MalformedURLException {

        final SubmitRequest submitRequest = createSubmitRequest("ERROR_305");
        final SubmitResponse submitResponse = cjseSimulatorTestUtil.getWebServicePort().submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(305, submitResponse.getResponseCode());
        Assert.assertEquals("WRONG DESTINATION", submitResponse.getResponseText());
    }

    @Test
    public void givenSoapRequestWithCorrelationIdError306SimulatorShouldReturnFailureMessageWith306ResponseCode() throws DatatypeConfigurationException, MalformedURLException {

        final SubmitRequest submitRequest = createSubmitRequest("ERROR_306");
        final SubmitResponse submitResponse = cjseSimulatorTestUtil.getWebServicePort().submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(306, submitResponse.getResponseCode());
        Assert.assertEquals("INVALID REQUEST ID", submitResponse.getResponseText());
    }

    @Test
    public void givenSoapRequestWithCorrelationIdError307SimulatorShouldReturnFailureMessageWith307ResponseCode() throws DatatypeConfigurationException, MalformedURLException {

        final SubmitRequest submitRequest = createSubmitRequest("ERROR_307");
        final SubmitResponse submitResponse = cjseSimulatorTestUtil.getWebServicePort().submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(307, submitResponse.getResponseCode());
        Assert.assertEquals("MODE ERROR", submitResponse.getResponseText());
    }

    private SubmitRequest createSubmitRequest(String correlationId) throws DatatypeConfigurationException {

        String newCorrleationIdTag = new StringBuilder("<CorrelationID>").append(correlationId).append("</CorrelationID>").toString();
        final String messageBody = TestData.XML_MESSAGE_UNESCAPED.replace("<CorrelationID>REQLUEST_AID</CorrelationID>", newCorrleationIdTag);
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("REQUEST_ID", messageBody);
        return submitRequest;
    }

    @Test
    public void testBasicWireMockSetup() {

        wireMockRule.stubFor(get(urlPathEqualTo("/some/message"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Hello world!")
                ));
        String endpointAddress = "http://localhost:8888/some/message";
        final RestClient restClient = new RestClient();
        Response response = restClient.query(endpointAddress, "text/xml");
        final String entity = response.readEntity(String.class);

        Assert.assertNotNull(response);
        assertTrue(entity.equalsIgnoreCase("Hello world!"));
    }

    private XMLGregorianCalendar getTimestamp() throws DatatypeConfigurationException {
        final GregorianCalendar now = new GregorianCalendar();
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
    }

}
