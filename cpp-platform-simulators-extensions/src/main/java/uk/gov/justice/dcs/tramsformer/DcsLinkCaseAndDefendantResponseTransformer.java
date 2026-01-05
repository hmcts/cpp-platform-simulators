package uk.gov.justice.dcs.tramsformer;

import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.http.protocol.HTTP.CONTENT_TYPE;
import static uk.gov.justice.dcs.tramsformer.DcsHelper.buildScenarioFailureResponse;
import static uk.gov.justice.dcs.tramsformer.DcsHelper.getErrorCodeList;

import uk.gov.hmcts.dcs.openapi.model.DefendantOrganisation;
import uk.gov.hmcts.dcs.openapi.model.DefendantPerson;
import uk.gov.hmcts.dcs.openapi.model.LinkCaseAndDefendantRequest;
import uk.gov.hmcts.dcs.openapi.model.RequestFulfilledResponsePayload;
import uk.gov.hmcts.dcs.openapi.model.RequestFulfilledResponsePayloadDefendantsInner;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DcsLinkCaseAndDefendantResponseTransformer extends ResponseDefinitionTransformer {

    public static final Logger LOGGER = LoggerFactory.getLogger(DcsLinkCaseAndDefendantResponseTransformer.class);

    private static final String UNDERSCORE = "_";

    public static final String DCS_LINK_CASE_AND_DEFENDANT_RESPONSE_TRANSFORMER = "dcs-link-case-and-defendant-response-transformer";

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {

        LOGGER.info("Transforming DCS link case and defendant response");
        LOGGER.info("Payload: {}", request.getBodyAsString());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            final LinkCaseAndDefendantRequest linkCaseAndDefendantRequest = objectMapper.readValue(request.getBodyAsString(), LinkCaseAndDefendantRequest.class);
            return buildResponse(linkCaseAndDefendantRequest);

        } catch (IOException e) {
            return buildFailureResponse();
        }

    }

    private ResponseDefinition buildResponse(final LinkCaseAndDefendantRequest request) throws JsonProcessingException {
        final List<String> defendantNames = request.getDefendants().stream()
                .map(defendant -> {
                    if (nonNull(defendant.getDefendantPerson())) {
                        return getPersonName(defendant.getDefendantPerson());
                    }
                    if (nonNull(defendant.getDefendantOrganisation())) {
                        return getOrganisationName(defendant.getDefendantOrganisation());
                    }
                    return EMPTY;
                }).toList();

        Optional<ResponseDefinition> anyErrorResponse = getErrorCodeList().stream()
                .filter(error -> defendantNames.stream().anyMatch(name -> name.toUpperCase().contains(error)))
                .map(error -> buildScenarioFailureResponse(error, error))
                .findFirst();
        if(defendantNames.stream().anyMatch(defendantName -> StringUtils.containsIgnoreCase(defendantName,"split_merged"))) {
            return buildScenarioFailureResponse("404", "CASE_HAS_SPLIT_OR_MERGED");
        }
        return anyErrorResponse.orElse(buildSuccessResponse(request));

    }

    private String getPersonName(final DefendantPerson person) {
        StringBuilder nameBuilder = new StringBuilder();
        if (isNotEmpty(person.getForename())) {
            nameBuilder.append(person.getForename().trim()).append(UNDERSCORE);
        }
        if (isNotEmpty(person.getMiddleName())) {
            nameBuilder.append(person.getMiddleName().trim()).append(UNDERSCORE);
        }
        return nameBuilder.append(isNotEmpty(person.getSurname()) ? person.getSurname().trim() : person.getSurname()).toString();
    }

    private String getOrganisationName(final DefendantOrganisation organisation) {
        if(isNotEmpty(organisation.getName())) {
            return organisation.getName().trim().replaceAll(StringUtils.SPACE, UNDERSCORE);
        }
        return EMPTY;
    }

    private ResponseDefinition buildSuccessResponse(final LinkCaseAndDefendantRequest request) throws JsonProcessingException {
        RequestFulfilledResponsePayload response = new RequestFulfilledResponsePayload();
        response.setCaseId(request.getCaseId());
        response.setTransactionRef(request.getTransactionRef());
        response.setCaseReferral(request.getCaseReferral() != null ? request.getCaseReferral() : randomUUID().toString());
        request.getDefendants().forEach(defendant -> {
            final RequestFulfilledResponsePayloadDefendantsInner defResponse = new RequestFulfilledResponsePayloadDefendantsInner();
            defResponse.setDefendantId(defendant.getId());
            defResponse.setDefendantReferral(randomUUID().toString());
            response.addDefendantsItem(defResponse);
        });

        return new ResponseDefinitionBuilder()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withStatus(201)
                .withBody(new ObjectMapper().writeValueAsString(response))
                .build();
    }


    private ResponseDefinition buildFailureResponse() {
        return new ResponseDefinitionBuilder()
                .withHeader("Content-Type", "application/json")
                .withStatus(400)
                .withBody(createObjectBuilder().add("errorMessage", "Unable to parse the payload").build().toString())
                .build();
    }

    @Override
    public String getName() {
        return DCS_LINK_CASE_AND_DEFENDANT_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }


}
