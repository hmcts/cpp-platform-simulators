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
import uk.gov.justice.cjse.transformer.CjseGetMessageByUrnAndProsecutorReferenceResponseTransformer;
import uk.gov.justice.cjse.transformer.CjseMessageReceivedResponseTransformer;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import cpp.platform.simulators.cjse.util.CjseSimulatorTestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CjseGetMessageByUrnAndProsecutorReferenceResponseTransformerTest {

    private CjseSimulatorTestUtil cjseSimulatorTestUtil;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .port(8888)
            .extensions(CjseMessageReceivedResponseTransformer.class, CjseGetMessageByUrnAndProsecutorReferenceResponseTransformer.class));

    @Before
    public void setupTest() throws IOException {
        cjseSimulatorTestUtil = new CjseSimulatorTestUtil();
        cjseSimulatorTestUtil.deleteFiles();
        wireMockRule.stubFor(post(urlPathEqualTo("/CJSE/message"))
                .willReturn(aResponse()
                        .withTransformer(CjseMessageReceivedResponseTransformer.CJSE_MESSAGE_RECEIVED_RESPONSE_TRANSFORMER, null, null)
                ));

        wireMockRule.stubFor(get(urlMatching("/newcase\\?urn=([\\da-zA-Z]*)&prosecutor_reference=(.*)$"))
                .willReturn(aResponse()
                        .withTransformer(CjseGetMessageByUrnAndProsecutorReferenceResponseTransformer.CJSE_GET_MESSAGE_BY_URN_AND_PROSECUTOR_REFERENCE_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @After
    public void tearDown() throws IOException {
        cjseSimulatorTestUtil.deleteFiles();
    }

    @Test
    public void givenValidUrnAndProsecutorReferenceThenCorrectFileWithThoseValuesShouldReturn_test_1() throws DatatypeConfigurationException, MalformedURLException {

        createFileWithId23Urn4321AndProsecutorReference1900XX0000000000061B();
        final RestClient restClient = new RestClient();
        String endpointAddress = getAbsoluteUrl("4321", "1900XX0000000000061B");
        Response response = restClient.query(endpointAddress, "text/xml", new MultivaluedHashMap<>());
        Assert.assertNotNull(response);
        final String entity = response.readEntity(String.class);
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity.contains("4321"));
        Assert.assertTrue(entity.contains("1900XX0000000000061B"));
    }


    @Test
    public void givenValidUrnAndProsecutorReferenceThenCorrectFileWithThoseValuesShouldReturn_test_2() throws DatatypeConfigurationException, MalformedURLException {

        createFileWithId22Urn42TK1000218AndProsecutorReference1900XX0000000000061V();
        final RestClient restClient = new RestClient();
        String endpointAddress = getAbsoluteUrl("42TK1000218", "1900XX0000000000061V");
        Response response = restClient.query(endpointAddress, "text/xml", new MultivaluedHashMap<>());
        Assert.assertNotNull(response);
        final String entity = response.readEntity(String.class);
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity.contains("42TK1000218"));
        Assert.assertTrue(entity.contains("1900XX0000000000061V"));
    }

    @Test
    public void givenMultipleFilesWithSameUrnAndProsecutorReferenceThenLatestFileWithThoseValuesShouldReturn()
            throws DatatypeConfigurationException, MalformedURLException, InterruptedException {

        createFileWithId20Urn1234AndProsecutorReference0416BH0000000090557N();
        Thread.sleep(2000);
        createFileWithId21Urn1234AndProsecutorReference0416BH0000000090557N();
        final RestClient restClient = new RestClient();
        String endpointAddress = getAbsoluteUrl("1234", "0416BH0000000090557N");
        Response response = restClient.query(endpointAddress, "text/xml", new MultivaluedHashMap<>());
        Assert.assertNotNull(response);
        final String entity = response.readEntity(String.class);
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity.contains("1234"));
        Assert.assertTrue(entity.contains("0416BH0000000090557N"));
        Assert.assertTrue(entity.contains("<RequestID>21</RequestID>"));
    }

    private void createFileWithId23Urn4321AndProsecutorReference1900XX0000000000061B() throws DatatypeConfigurationException, MalformedURLException {

        CJSEPort cjsePort = cjseSimulatorTestUtil.getWebServicePort();
        setDataWithUrnAndProsecutorReference("4321", "1900XX0000000000061B");
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("23", ModifiableTestData.getData());
        final SubmitResponse submitResponse = cjsePort.submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }

    private void createFileWithId22Urn42TK1000218AndProsecutorReference1900XX0000000000061V() throws DatatypeConfigurationException, MalformedURLException {

        CJSEPort cjsePort = cjseSimulatorTestUtil.getWebServicePort();
        setDataWithUrnAndProsecutorReference("42TK1000218", "1900XX0000000000061V");
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("22", ModifiableTestData.getData());
        final SubmitResponse submitResponse = cjsePort.submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }

    private void createFileWithId21Urn1234AndProsecutorReference0416BH0000000090557N() throws DatatypeConfigurationException, MalformedURLException {

        CJSEPort cjsePort = cjseSimulatorTestUtil.getWebServicePort();
        setDataWithUrnAndProsecutorReference("1234", "0416BH0000000090557N");
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("21", ModifiableTestData.getData());
        final SubmitResponse submitResponse = cjsePort.submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }

    private void createFileWithId20Urn1234AndProsecutorReference0416BH0000000090557N() throws DatatypeConfigurationException, MalformedURLException {

        CJSEPort cjsePort = cjseSimulatorTestUtil.getWebServicePort();
        setDataWithUrnAndProsecutorReference("1234", "0416BH0000000090557N");
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("20", ModifiableTestData.getData());
        final SubmitResponse submitResponse = cjsePort.submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }

    private void setDataWithUrnAndProsecutorReference(final String urn, final String prosecutorReference) {
        ModifiableTestData.setValueForField("PTIURN", urn);
        ModifiableTestData.setValueForField("ProsecutorReference", prosecutorReference);
    }


    private String getAbsoluteUrl(final String urn, final String prosecutorReference) {
        return cjseSimulatorTestUtil.getBaseUri() + "/newcase?urn=" + urn + "&prosecutor_reference=" + prosecutorReference;
    }

}
