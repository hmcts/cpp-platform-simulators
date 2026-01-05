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
import static uk.gov.justice.dcs.tramsformer.DcsDefendantUpdateResponseTransformer.DCS_DEFENDANT_UPDATE_RESPONSE_TRANSFORMER;

import uk.gov.hmcts.dcs.openapi.model.Defendant;
import uk.gov.hmcts.dcs.openapi.model.DefendantOrganisation;
import uk.gov.hmcts.dcs.openapi.model.DefendantPerson;
import uk.gov.hmcts.dcs.openapi.model.UpdateDefendantDetailsRequest;
import uk.gov.justice.dcs.tramsformer.DcsDefendantUpdateResponseTransformer;
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

public class DcsDefendantUpdateResponseTransformerTest {

    public static final String PROBLEM = "PROBLEM";
    private static final String DCS_OUTBOUND_DEFENDANT_UPDATE = "/CP/dcs-outbound/v1/case/([^/]+)/defendant/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888).extensions(DcsDefendantUpdateResponseTransformer.class));
    private String caseUrn;

    @Before
    public void setupTest() {

        caseUrn = randomAlphanumeric(8);
        wireMockRule.stubFor(post(urlPathMatching(DCS_OUTBOUND_DEFENDANT_UPDATE))
                .willReturn(aResponse()
                        .withTransformer(DCS_DEFENDANT_UPDATE_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @Test
    public void shouldReturnValidResponseForDefendantUpdateRequest() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s", caseUrn, randomUUID());

        UpdateDefendantDetailsRequest updateDefendantDetailsRequest = buildRequest();

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantDetailsRequest)));
        assertThat(response.getStatus(), is(204));
    }

    @Test
    public void shouldReturnBadRequestResponseForInvalidPayload() {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s", caseUrn, randomUUID());

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress).request(MediaType.APPLICATION_JSON).post(Entity.json("This payload is invalid"));
        assertThat(response.getStatus(), is(400));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("Unable to parse the payload"));
    }

    @Test
    public void shouldReturnErrorResponseForValidDefendantUpdatedRequest_ForBadRequest() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s", caseUrn, randomUUID());

        DefendantPerson defendantPerson = new DefendantPerson();
        defendantPerson.setForename("bad");
        defendantPerson.setMiddleName("request");
        defendantPerson.setSurname("anything");

        UpdateDefendantDetailsRequest updateDefendantDetailsRequest = buildRequestForScenarioError(defendantPerson, null);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantDetailsRequest)));
        assertThat(response.getStatus(), is(400));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("BAD_REQUEST"));
        assertThat(responseObject.containsKey("errorCode"), is(false));
    }

    @Test
    public void shouldReturnErrorResponseForValidDefendantUpdatedRequest_ForInvalidServerError() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s", caseUrn, randomUUID());

        DefendantPerson defendantPerson = new DefendantPerson();
        defendantPerson.setForename("internal");
        defendantPerson.setMiddleName("server");
        defendantPerson.setSurname("error");

        UpdateDefendantDetailsRequest updateDefendantDetailsRequest = buildRequestForScenarioError(defendantPerson, null);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantDetailsRequest)));
        assertThat(response.getStatus(), is(500));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("INTERNAL_SERVER_ERROR"));
        assertThat(responseObject.containsKey("errorCode"), is(false));
    }

    @Test
    public void shouldReturnErrorResponseForValidDefendantUpdatedRequest_ForCaseDeletedError() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s", caseUrn, randomUUID());

        DefendantPerson defendantPerson = new DefendantPerson();
        defendantPerson.setForename("case");
        defendantPerson.setSurname("deleted");

        UpdateDefendantDetailsRequest updateDefendantDetailsRequest = buildRequestForScenarioError(defendantPerson, null);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantDetailsRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_DELETED"));
        assertThat(responseObject.getString("errorCode"), is("CASE_DELETED"));
    }

    @Test
    public void shouldReturnErrorResponseForValidDefendantUpdatedRequest_ForCaseSplitMergedError() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s", caseUrn, randomUUID());

        DefendantPerson defendantPerson = new DefendantPerson();
        defendantPerson.setForename("split");
        defendantPerson.setSurname("merged");

        UpdateDefendantDetailsRequest updateDefendantDetailsRequest = buildRequestForScenarioError(defendantPerson, null);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantDetailsRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_HAS_SPLIT_OR_MERGED"));
        assertThat(responseObject.getString("errorCode"), is("404"));
    }

    @Test
    public void shouldReturnErrorResponseForValidDefendantUpdatedRequest_ForCaseNotFound() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s", caseUrn, randomUUID());

        DefendantOrganisation defendantOrganisation = new DefendantOrganisation();
        defendantOrganisation.setName("case not found ltd");

        UpdateDefendantDetailsRequest updateDefendantDetailsRequest = buildRequestForScenarioError(null, defendantOrganisation);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantDetailsRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_NOT_FOUND"));
        assertThat(responseObject.getString("errorCode"), is("CASE_NOT_FOUND"));
    }

    @Test
    public void shouldReturnErrorResponseForValidDefendantUpdatedRequest_ForAnyError() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendant/%s", caseUrn, randomUUID());

        DefendantOrganisation defendantOrganisation = new DefendantOrganisation();
        defendantOrganisation.setName("any freaking problem ltd");

        UpdateDefendantDetailsRequest updateDefendantDetailsRequest = buildRequestForScenarioError(null, defendantOrganisation);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(updateDefendantDetailsRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), containsString(PROBLEM));
        assertThat(responseObject.getString("errorCode"), containsString(PROBLEM));
    }

    private UpdateDefendantDetailsRequest buildRequest() {
        UpdateDefendantDetailsRequest updateDefendantDetailsRequest = new UpdateDefendantDetailsRequest();
        updateDefendantDetailsRequest.setTransactionRef(randomUUID().toString());
        updateDefendantDetailsRequest.setCaseId(randomUUID().toString());
        updateDefendantDetailsRequest.setCaseReferral(randomUUID().toString());

        Defendant defendant = new Defendant();
        defendant.setId(randomUUID().toString());
        DefendantPerson person = new DefendantPerson();
        person.setForename(randomAlphanumeric(15));
        person.setSurname(randomAlphanumeric(15));
        person.setDateOfBirth("1980-01-01");
        defendant.setDefendantPerson(person);

        updateDefendantDetailsRequest.setDefendant(defendant);
        return updateDefendantDetailsRequest;
    }

    private UpdateDefendantDetailsRequest buildRequestForScenarioError(final DefendantPerson person , final DefendantOrganisation organisation) {
        UpdateDefendantDetailsRequest updateDefendantDetailsRequest = buildRequest();
        updateDefendantDetailsRequest.getDefendant().setDefendantOrganisation(organisation);
        updateDefendantDetailsRequest.getDefendant().setDefendantPerson(person);

        return updateDefendantDetailsRequest;
    }

}