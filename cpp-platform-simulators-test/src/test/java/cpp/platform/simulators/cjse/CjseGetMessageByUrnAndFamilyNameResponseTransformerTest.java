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
import uk.gov.justice.cjse.transformer.CjseGetMessageByUrnAndFamilyNameResponseTransformer;
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

public class CjseGetMessageByUrnAndFamilyNameResponseTransformerTest {

    private CjseSimulatorTestUtil cjseSimulatorTestUtil;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .port(8888)
            .extensions(CjseMessageReceivedResponseTransformer.class, CjseGetMessageByUrnAndFamilyNameResponseTransformer.class));

    @Before
    public void setupTest() throws IOException {
        cjseSimulatorTestUtil = new CjseSimulatorTestUtil();
        cjseSimulatorTestUtil.deleteFiles();
        wireMockRule.stubFor(post(urlPathEqualTo("/CJSE/message"))
                .willReturn(aResponse()
                        .withTransformer(CjseMessageReceivedResponseTransformer.CJSE_MESSAGE_RECEIVED_RESPONSE_TRANSFORMER, null, null)
                ));

        wireMockRule.stubFor(get(urlMatching("/newcase\\?urn=([\\da-zA-Z]*)&family_name=([a-zA-Z]*)$"))
                .willReturn(aResponse()
                        .withTransformer(CjseGetMessageByUrnAndFamilyNameResponseTransformer.CJSE_GET_MESSAGE_BY_URN_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @After
    public void tearDown() throws IOException {
        cjseSimulatorTestUtil.deleteFiles();
    }

    @Test
    public void givenValidUrnAndFamilyNameThenCorrectFileWithThoseValuesShouldReturn_test_1() throws DatatypeConfigurationException, MalformedURLException {

        createFileWithId13Urn4321AndFamilyNameHurley();
        final RestClient restClient = new RestClient();
        String endpointAddress = getAbsoluteUrl("4321", "Hurley");
        Response response = restClient.query(endpointAddress, "text/xml", new MultivaluedHashMap<>());
        Assert.assertNotNull(response);
        final String entity = response.readEntity(String.class);
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity.contains("4321"));
        Assert.assertTrue(entity.contains("Hurley"));
    }


    @Test
    public void givenValidUrnAndFamilyNameThenCorrectFileWithThoseValuesShouldReturn_test_2() throws DatatypeConfigurationException, MalformedURLException {

        createFileWithId14Urn42TK1000218AndFamilyNamePaul();
        final RestClient restClient = new RestClient();
        String endpointAddress = getAbsoluteUrl("42TK1000218", "Paul");
        Response response = restClient.query(endpointAddress, "text/xml", new MultivaluedHashMap<>());
        Assert.assertNotNull(response);
        final String entity = response.readEntity(String.class);
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity.contains("42TK1000218"));
        Assert.assertTrue(entity.contains("PAUL"));
    }

    @Test
    public void givenMultipleFilesWithSameUrnAndFamilyNameThenLatestFileWithThoseValuesShouldReturn()
            throws DatatypeConfigurationException, MalformedURLException, InterruptedException {

        createFileWithId11Urn1234AndFamilyNameSmith();
        Thread.sleep(2000);
        createFileWithId12Urn1234AndFamilyNameSmith();
        final RestClient restClient = new RestClient();
        String endpointAddress = getAbsoluteUrl("1234", "Smith");
        Response response = restClient.query(endpointAddress, "text/xml", new MultivaluedHashMap<>());
        Assert.assertNotNull(response);
        final String entity = response.readEntity(String.class);
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity.contains("1234"));
        Assert.assertTrue(entity.contains("SMITH"));
        Assert.assertTrue(entity.contains("<RequestID>12</RequestID>"));
    }

    private void createFileWithId14Urn42TK1000218AndFamilyNamePaul() throws DatatypeConfigurationException, MalformedURLException {

        CJSEPort cjsePort = cjseSimulatorTestUtil.getWebServicePort();
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("13", TestData.MESSAGE_WITH_URN_42TK1000218_AND_FAMILY_NAME_PAUL);
        final SubmitResponse submitResponse = cjsePort.submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }

    private void createFileWithId13Urn4321AndFamilyNameHurley() throws DatatypeConfigurationException, MalformedURLException {

        CJSEPort cjsePort = cjseSimulatorTestUtil.getWebServicePort();
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("13", TestData.MESSAGE_WITH_URN_4321_AND_FAMILY_NAME_HURLEY);
        final SubmitResponse submitResponse = cjsePort.submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }

    private void createFileWithId12Urn1234AndFamilyNameSmith() throws DatatypeConfigurationException, MalformedURLException {

        CJSEPort cjsePort = cjseSimulatorTestUtil.getWebServicePort();
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("12", TestData.MESSAGE_WITH_URN_1234_AND_FAMILY_NAME_SMITH);
        final SubmitResponse submitResponse = cjsePort.submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }

    private void createFileWithId11Urn1234AndFamilyNameSmith() throws DatatypeConfigurationException, MalformedURLException {

        CJSEPort cjsePort = cjseSimulatorTestUtil.getWebServicePort();
        SubmitRequest submitRequest = cjseSimulatorTestUtil.createSoapRequest("11", TestData.MESSAGE_WITH_URN_1234_AND_FAMILY_NAME_SMITH);
        final SubmitResponse submitResponse = cjsePort.submit(submitRequest);
        Assert.assertNotNull(submitResponse);
        Assert.assertEquals(1, submitResponse.getResponseCode());
        Assert.assertEquals("SUCCESS", submitResponse.getResponseText());
    }


    private String getAbsoluteUrl(final String urn, final String familyName) {
        return cjseSimulatorTestUtil.getBaseUri() + "/newcase?urn=" + urn + "&family_name=" + familyName;
    }

}
