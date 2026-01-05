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
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.dcs.openapi.model.LinkCaseAndDefendantRequest.ProsecutedByEnum.CPS;
import static uk.gov.justice.dcs.tramsformer.DcsLinkCaseAndDefendantResponseTransformer.DCS_LINK_CASE_AND_DEFENDANT_RESPONSE_TRANSFORMER;

import uk.gov.hmcts.dcs.openapi.model.Defendant;
import uk.gov.hmcts.dcs.openapi.model.DefendantOrganisation;
import uk.gov.hmcts.dcs.openapi.model.DefendantPerson;
import uk.gov.hmcts.dcs.openapi.model.LinkCaseAndDefendantRequest;
import uk.gov.hmcts.dcs.openapi.model.RequestFulfilledResponsePayload;
import uk.gov.justice.dcs.tramsformer.DcsLinkCaseAndDefendantResponseTransformer;
import uk.gov.justice.services.test.utils.core.rest.ResteasyClientBuilderFactory;

import java.io.IOException;
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

public class DcsLinkCaseAndDefendantResponseTransformerTest {

    public static final String PROBLEM = "PROBLEM";
    private static final String DCS_OUTBOUND_LINK_CASE_AND_DEFENDANT = "/CP/dcs-outbound/v1/case/([^/]+)/defendants";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888).extensions(DcsLinkCaseAndDefendantResponseTransformer.class));
    private String caseUrn;

    @Before
    public void setupTest() throws IOException {

        caseUrn = randomAlphanumeric(8);
        wireMockRule.stubFor(post(urlPathMatching(DCS_OUTBOUND_LINK_CASE_AND_DEFENDANT))
                .willReturn(aResponse()
                        .withTransformer(DCS_LINK_CASE_AND_DEFENDANT_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @Test
    public void shouldReturnValidResponseForValidCaseAndDefendantRequest() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendants", caseUrn);

        LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = buildRequest();

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(linkCaseAndDefendantRequest)));
        assertThat(response.getStatus(), is(201));

        final RequestFulfilledResponsePayload responseObject = response.readEntity(RequestFulfilledResponsePayload.class);
        assertThat(responseObject.getTransactionRef(), is(linkCaseAndDefendantRequest.getTransactionRef()));
        assertNotNull(responseObject.getCaseReferral());
        assertThat(responseObject.getDefendants().get(0).getDefendantId(), is(linkCaseAndDefendantRequest.getDefendants().get(0).getId()));
        assertNotNull(responseObject.getDefendants().get(0).getDefendantReferral());
    }

    @Test
    public void shouldReturnBadRequestResponseForInvalidPayload() {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendants", caseUrn);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress).request(MediaType.APPLICATION_JSON).post(Entity.json("This payload is invalid"));
        assertThat(response.getStatus(), is(400));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("Unable to parse the payload"));
    }

    @Test
    public void shouldReturnErrorResponseForValidCaseAndDefendantRequest_ForCaseAlreadyExists() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendants", caseUrn);

        DefendantPerson defendantPerson = new DefendantPerson();
        defendantPerson.setForename("case");
        defendantPerson.setMiddleName("already");
        defendantPerson.setSurname("exists");

        LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = buildRequestForScenarioError(defendantPerson, null);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(linkCaseAndDefendantRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_ALREADY_EXISTS"));
        assertThat(responseObject.getString("errorCode"), is("CASE_ALREADY_EXISTS"));
    }

    @Test
    public void shouldReturnErrorResponseForValidCaseAndDefendantRequest_ForCaseDeleted() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendants", caseUrn);

        DefendantPerson defendantPerson = new DefendantPerson();
        defendantPerson.setForename("case");
        defendantPerson.setSurname("deleted");

        LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = buildRequestForScenarioError(defendantPerson, null);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(linkCaseAndDefendantRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_DELETED"));
        assertThat(responseObject.getString("errorCode"), is("CASE_DELETED"));
    }
    @Test
    public void shouldReturnErrorResponseForValidCaseAndDefendantRequest_ForCaseSplitMerged() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendants", caseUrn);

        DefendantPerson defendantPerson = new DefendantPerson();
        defendantPerson.setForename("split");
        defendantPerson.setSurname("merged");

        LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = buildRequestForScenarioError(defendantPerson, null);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(linkCaseAndDefendantRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("CASE_HAS_SPLIT_OR_MERGED"));
        assertThat(responseObject.getString("errorCode"), is("404"));
    }

    @Test
    public void shouldReturnErrorResponseForValidCaseAndDefendantRequest_ForDefendantAlreadyExists() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendants", caseUrn);

        DefendantOrganisation defendantOrganisation = new DefendantOrganisation();
        defendantOrganisation.setName("defendant already exists ltd");

        LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = buildRequestForScenarioError(null, defendantOrganisation);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(linkCaseAndDefendantRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("DEFENDANT_ALREADY_EXISTS"));
        assertThat(responseObject.getString("errorCode"), is("DEFENDANT_ALREADY_EXISTS"));
    }

    @Test
    public void shouldReturnErrorResponseForValidCaseAndDefendantRequest_ForBadRequest() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendants", caseUrn);

        DefendantOrganisation defendantOrganisation = new DefendantOrganisation();
        defendantOrganisation.setName("bad request ltd abc");

        LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = buildRequestForScenarioError(null, defendantOrganisation);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(linkCaseAndDefendantRequest)));
        assertThat(response.getStatus(), is(400));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("BAD_REQUEST"));
        assertThat(responseObject.containsKey("errorCode"), is(false));
    }

    @Test
    public void shouldReturnErrorResponseForValidCaseAndDefendantRequest_ForInvalidServerError() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendants", caseUrn);

        DefendantOrganisation defendantOrganisation = new DefendantOrganisation();
        defendantOrganisation.setName("internal server error ltd abc");

        LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = buildRequestForScenarioError(null, defendantOrganisation);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(linkCaseAndDefendantRequest)));
        assertThat(response.getStatus(), is(500));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), is("INTERNAL_SERVER_ERROR"));
        assertThat(responseObject.containsKey("errorCode"), is(false));
    }

    @Test
    public void shouldReturnErrorResponseForValidCaseAndDefendantRequest_ForAnyError() throws JsonProcessingException {
        String endpointAddress = String.format("http://localhost:8888" + "/CP/dcs-outbound/v1/case/%s/defendants", caseUrn);

        DefendantOrganisation defendantOrganisation = new DefendantOrganisation();
        defendantOrganisation.setName("any problem error ltd");

        LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = buildRequestForScenarioError(null, defendantOrganisation);

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new ObjectMapper().writeValueAsString(linkCaseAndDefendantRequest)));
        assertThat(response.getStatus(), is(404));

        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("errorMessage"), containsString(PROBLEM));
        assertThat(responseObject.getString("errorCode"), containsString(PROBLEM));
    }

    private LinkCaseAndDefendantRequest buildRequest() {
        LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = new LinkCaseAndDefendantRequest();
        linkCaseAndDefendantRequest.setTransactionRef(randomUUID().toString());
        linkCaseAndDefendantRequest.setCaseId(randomUUID().toString());
        linkCaseAndDefendantRequest.setProsecutedBy(CPS);

        Defendant defendant = new Defendant();
        defendant.setId(randomUUID().toString());
        DefendantPerson person = new DefendantPerson();
        person.setForename(randomAlphanumeric(15));
        person.setSurname(randomAlphanumeric(15));
        person.setDateOfBirth("1980-01-01");
        defendant.setDefendantPerson(person);

        linkCaseAndDefendantRequest.addDefendantsItem(defendant);
        return linkCaseAndDefendantRequest;
    }

    private LinkCaseAndDefendantRequest buildRequestForScenarioError(final DefendantPerson person , final DefendantOrganisation organisation) {
        LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = buildRequest();
        linkCaseAndDefendantRequest.getDefendants().forEach(defendant -> {
            defendant.setDefendantPerson(person);
            defendant.setDefendantOrganisation(organisation);
        });

        return linkCaseAndDefendantRequest;
    }

}