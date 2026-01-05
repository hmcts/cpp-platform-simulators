package cpp.platform.simulators.dcs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.dcs.tramsformer.DcsMaterialUpdateResponseTransformer.DCS_MATERIAL_UPDATE_RESPONSE_TRANSFORMER;

import uk.gov.hmcts.dcs.openapi.model.AddCaseMaterialRequest;
import uk.gov.justice.dcs.tramsformer.DcsMaterialUpdateResponseTransformer;
import uk.gov.justice.services.test.utils.core.rest.ResteasyClientBuilderFactory;

import java.io.StringReader;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DcsMaterialUpdateResponseTransformerTest {

    private static final String DCS_OUTBOUND_MATERIAL_UPDATE = "/CP/dcs-outbound/v1/case/([^/]+)/material";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888).extensions(DcsMaterialUpdateResponseTransformer.class));
    private String caseUrn;

    @Before
    public void setupTest() {

        caseUrn = randomAlphanumeric(8);
        wireMockRule.stubFor(post(urlPathMatching(DCS_OUTBOUND_MATERIAL_UPDATE))
                .willReturn(aResponse()
                        .withTransformer(DCS_MATERIAL_UPDATE_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @Test
    public void shouldReturnValidResponseForMaterialUpdateRequest() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        AddCaseMaterialRequest addCaseMaterialRequest = buildRequest();

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(addCaseMaterialRequest)));
        assertThat(response.getStatus(), is(202));
    }

    @Test
    public void shouldReturnBadRequestResponseForInvalidPayload() {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress).request(MediaType.APPLICATION_JSON).post(Entity.json("This payload is invalid"));
        assertThat(response.getStatus(), is(400));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("Unable to parse the payload"));
    }

    @Test
    public void shouldReturnErrorResponseForValidMaterialUpdateRequest_ForBadRequest() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        AddCaseMaterialRequest addCaseMaterialRequest = buildRequestForScenarioError("Exhibits");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(addCaseMaterialRequest)));
        assertThat(response.getStatus(), is(400));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("BAD_REQUEST"));
        assertThat(responseObject.containsKey("errorCode"), is(false));
    }
    @Test
    public void shouldReturnErrorResponseForValidMaterialUpdateRequest_ForCaseDeleted() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        AddCaseMaterialRequest addCaseMaterialRequest = buildRequestForScenarioError("Sentence");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(addCaseMaterialRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_DELETED"));
        assertThat(responseObject.containsKey("errorCode"), is(true));
    }
    @Test
    public void shouldReturnErrorResponseForValidMaterialUpdateRequest_ForCaseSplitMerged() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        AddCaseMaterialRequest addCaseMaterialRequest = buildRequestForScenarioError("Representation");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(addCaseMaterialRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_HAS_SPLIT_OR_MERGED"));
        assertThat(responseObject.containsKey("errorCode"), is(true));
    }

    @Test
    public void shouldReturnErrorResponseForValidMaterialUpdateRequest_ForInvalidServerError() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        AddCaseMaterialRequest addCaseMaterialRequest = buildRequestForScenarioError("Transcripts ABE interviews");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(addCaseMaterialRequest)));
        assertThat(response.getStatus(), is(500));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("INTERNAL_SERVER_ERROR"));
        assertThat(responseObject.containsKey("errorCode"), is(false));
    }

    @Test
    public void shouldReturnErrorResponseForValidMaterialUpdateRequest_ForCaseNotFound() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        AddCaseMaterialRequest addCaseMaterialRequest = buildRequestForScenarioError("Witness Statements");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(addCaseMaterialRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_NOT_FOUND"));
        assertThat(responseObject.getString("errorCode"), is("CASE_NOT_FOUND"));
    }

    @Test
    public void shouldReturnErrorResponseForValidMaterialUpdateRequest_ForDocumentNotUploaded() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        AddCaseMaterialRequest addCaseMaterialRequest = buildRequestForScenarioError("Indictment");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(addCaseMaterialRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("DOCUMENT_NOT_UPLOADED"));
        assertThat(responseObject.getString("errorCode"), is("DOCUMENT_NOT_UPLOADED"));
    }

    @Test
    public void shouldReturnErrorResponseForValidMaterialUpdateRequest_ForDocumentContainsVirus() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        AddCaseMaterialRequest addCaseMaterialRequest = buildRequestForScenarioError("Key witness Statements");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(addCaseMaterialRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("DOCUMENT_CONTAINS_VIRUS"));
        assertThat(responseObject.getString("errorCode"), is("DOCUMENT_CONTAINS_VIRUS"));
    }

    @Test
    public void shouldReturnErrorResponseForValidMaterialUpdateRequest_ForMaterialNotAccepted() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        AddCaseMaterialRequest addCaseMaterialRequest = buildRequestForScenarioError("Case Summary");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(addCaseMaterialRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("MATERIAL_NOT_ACCEPTED"));
        assertThat(responseObject.getString("errorCode"), is("MATERIAL_NOT_ACCEPTED"));
    }

    @Test
    public void shouldReturnErrorResponseForValidMaterialUpdateRequest_ForDefendantNotFound() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/material", caseUrn);

        AddCaseMaterialRequest addCaseMaterialRequest = buildRequestForScenarioError("Trial Documents");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(addCaseMaterialRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("DEFENDANT_NOT_FOUND"));
        assertThat(responseObject.getString("errorCode"), is("DEFENDANT_NOT_FOUND"));
    };

    private AddCaseMaterialRequest buildRequest() {
        AddCaseMaterialRequest addCaseMaterialRequest = new AddCaseMaterialRequest();
        addCaseMaterialRequest.setTransactionRef(randomUUID().toString());
        addCaseMaterialRequest.setCaseId(randomUUID().toString());
        addCaseMaterialRequest.setCaseReferral(randomUUID().toString());
        addCaseMaterialRequest.setMaterialUrl("http://dchost.pkg.uk/CP/dcs-outbound/v1/case/DFK48645JKDM/material");
        addCaseMaterialRequest.setDocumentDate("2025-07-25");
        addCaseMaterialRequest.setDocumentName("name.pdf");
        addCaseMaterialRequest.setDocumentSection("Charges");
        addCaseMaterialRequest.setUploadedByUser("erica willson");
        return addCaseMaterialRequest;
    }

    private AddCaseMaterialRequest buildRequestForScenarioError(final String sectionName) {
        AddCaseMaterialRequest addCaseMaterialRequest = buildRequest();
        addCaseMaterialRequest.setDocumentSection(sectionName);
        return addCaseMaterialRequest;
    }

}