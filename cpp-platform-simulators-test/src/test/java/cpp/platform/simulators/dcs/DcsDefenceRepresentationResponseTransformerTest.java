package cpp.platform.simulators.dcs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.dcs.tramsformer.DcsDefenceRepresentationResponseTransformer.DCS_DEFENCE_REPRESENTATION_RESPONSE_TRANSFORMER;

import uk.gov.hmcts.dcs.openapi.model.DefenceRepresentation;
import uk.gov.hmcts.dcs.openapi.model.UpdateDefendantRepresentationRequest;
import uk.gov.justice.dcs.tramsformer.DcsDefenceRepresentationResponseTransformer;
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

public class DcsDefenceRepresentationResponseTransformerTest {

    private static final String DCS_OUTBOUND_DEFENCE_REPRESENTATION_UPDATE = "/CP/dcs-outbound/v1/case/([^/]+)/defendant/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/defenceRepresentation";
    public static final String PROBLEM = "PROBLEM";
    public static final String BAD_REQUEST = "BAD_REQUEST";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888).extensions(DcsDefenceRepresentationResponseTransformer.class));
    private String caseUrn;

    @Before
    public void setupTest() {

        caseUrn = randomAlphanumeric(8);
        wireMockRule.stubFor(post(urlPathMatching(DCS_OUTBOUND_DEFENCE_REPRESENTATION_UPDATE))
                .willReturn(aResponse()
                        .withTransformer(DCS_DEFENCE_REPRESENTATION_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @Test
    public void shouldReturnValidResponseForDefenceRepresentationRequest() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s/defenceRepresentation", caseUrn, randomUUID());

        UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = buildRequest();

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantRepresentationRequest)));
        assertThat(response.getStatus(), is(204));
    }

    @Test
    public void shouldReturnBadRequestResponseForInvalidPayload() {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s/defenceRepresentation", caseUrn, randomUUID());

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress).request(MediaType.APPLICATION_JSON).post(Entity.json("This payload is invalid"));
        assertThat(response.getStatus(), is(400));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("Unable to parse the payload"));
    }

    @Test
    public void shouldReturnErrorResponseForValidDefenceRepresentationRequest_ForBadRequest() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s/defenceRepresentation", caseUrn, randomUUID());

        UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = buildRequestForScenarioError(UpdateDefendantRepresentationRequest.ActionEnum.CREATE, "defence org llp bad request ltd");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantRepresentationRequest)));
        assertThat(response.getStatus(), is(400));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is(BAD_REQUEST));
        assertThat(responseObject.containsKey("errorCode"), is(false));
    }

    @Test
    public void shouldReturnErrorResponseForValidDefenceRepresentationRequest_ForInvalidServerError() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s/defenceRepresentation", caseUrn, randomUUID());

        UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = buildRequestForScenarioError(UpdateDefendantRepresentationRequest.ActionEnum.CREATE, "defence org llp internal server error ltd");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantRepresentationRequest)));
        assertThat(response.getStatus(), is(500));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("INTERNAL_SERVER_ERROR"));
        assertThat(responseObject.containsKey("errorCode"), is(false));
    }


    @Test
    public void shouldReturnErrorResponseForValidDefenceRepresentationRequest_ForCaseNotFound() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s/defenceRepresentation", caseUrn, randomUUID());

        UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = buildRequestForScenarioError(UpdateDefendantRepresentationRequest.ActionEnum.CREATE, "defence org llp case not found ltd");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantRepresentationRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_NOT_FOUND"));
        assertThat(responseObject.getString("errorCode"), is("CASE_NOT_FOUND"));
    }
    @Test
    public void shouldReturnErrorResponseForValidDefenceRepresentationRequest_ForCaseDeleted() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s/defenceRepresentation", caseUrn, randomUUID());

        UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = buildRequestForScenarioError(UpdateDefendantRepresentationRequest.ActionEnum.CREATE, "defence org llp case deleted ltd");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantRepresentationRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_DELETED"));
        assertThat(responseObject.getString("errorCode"), is("CASE_DELETED"));
    }

    @Test
    public void shouldReturnErrorResponseForValidDefenceRepresentationRequest_ForDefenceRepresentationError() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s/defenceRepresentation", caseUrn, randomUUID());

        UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = buildRequestForScenarioError(UpdateDefendantRepresentationRequest.ActionEnum.CREATE, "defence_org_llp_case_defence_rep_error");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantRepresentationRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("DEFENCE_REPRESENTATION_ERROR"));
        assertThat(responseObject.getString("errorCode"), is("404"));
    }
    @Test
    public void shouldReturnErrorResponseForValidDefenceRepresentationRequest_ForCaseSplitMerged() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s/defenceRepresentation", caseUrn, randomUUID());

        UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = buildRequestForScenarioError(UpdateDefendantRepresentationRequest.ActionEnum.CREATE, "defence org llp case split or merged ltd");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantRepresentationRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_HAS_SPLIT_OR_MERGED"));
        assertThat(responseObject.getString("errorCode"), is("404"));
    }

    @Test
    public void shouldReturnErrorResponseForValidDefenceRepresentationRequest_ForAnyError() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s/defenceRepresentation", caseUrn, randomUUID());

        UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = buildRequestForScenarioError(UpdateDefendantRepresentationRequest.ActionEnum.CREATE, "defence org llp "+PROBLEM+" ltd");

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantRepresentationRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), containsString(PROBLEM));
        assertThat(responseObject.getString("errorCode"), containsString(PROBLEM));
    }

    private UpdateDefendantRepresentationRequest buildRequest() {
        UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = new UpdateDefendantRepresentationRequest();
        updateDefendantRepresentationRequest.setTransactionRef(randomUUID().toString());
        updateDefendantRepresentationRequest.setCaseId(randomUUID().toString());
        updateDefendantRepresentationRequest.setAction(UpdateDefendantRepresentationRequest.ActionEnum.CREATE);

        DefenceRepresentation representation = new DefenceRepresentation();
        representation.setEmail("email@email.com");
        representation.setOrganisationName("valid org name ltd");

        updateDefendantRepresentationRequest.setDefenceRepresentation(representation);
        return updateDefendantRepresentationRequest;
    }

    private UpdateDefendantRepresentationRequest buildRequestForScenarioError(final UpdateDefendantRepresentationRequest.ActionEnum action, final String orgName) {
        UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = new UpdateDefendantRepresentationRequest();
        updateDefendantRepresentationRequest.setTransactionRef(randomUUID().toString());
        updateDefendantRepresentationRequest.setCaseId(randomUUID().toString());
        updateDefendantRepresentationRequest.setAction(action);

        DefenceRepresentation representation = new DefenceRepresentation();
        representation.setEmail("email@email.com");
        representation.setOrganisationName(orgName);

        updateDefendantRepresentationRequest.setDefenceRepresentation(representation);
        return updateDefendantRepresentationRequest;
    }

}