package uk.gov.justice.dcs.tramsformer;

import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.http.protocol.HTTP.CONTENT_TYPE;
import static uk.gov.justice.dcs.tramsformer.DcsHelper.buildScenarioFailureResponse;
import static uk.gov.justice.dcs.tramsformer.DcsHelper.getErrorCodeList;

import uk.gov.hmcts.dcs.openapi.model.DefenceRepresentation;
import uk.gov.hmcts.dcs.openapi.model.UpdateDefendantRepresentationRequest;

import java.io.IOException;
import java.util.Optional;

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

public class DcsDefenceRepresentationResponseTransformer extends ResponseDefinitionTransformer {

    public static final Logger LOGGER = LoggerFactory.getLogger(DcsDefenceRepresentationResponseTransformer.class);

    private static final String UNDERSCORE = "_";

    public static final String DCS_DEFENCE_REPRESENTATION_RESPONSE_TRANSFORMER = "dcs-defence-representation-response-transformer";

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {

        LOGGER.info("Transforming DCS defence representation response");
        LOGGER.info("Payload: {}", request.getBodyAsString());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            final UpdateDefendantRepresentationRequest updateDefendantRepresentationRequest = objectMapper.readValue(request.getBodyAsString(), UpdateDefendantRepresentationRequest.class);
            return buildResponse(updateDefendantRepresentationRequest);

        } catch (IOException e) {
            return buildFailureResponse();
        }

    }

    private ResponseDefinition buildResponse(final UpdateDefendantRepresentationRequest request) {
        final DefenceRepresentation updateDefendantRepresentationRequestDefenceRepresentation = request.getDefenceRepresentation();
        StringBuilder nameBuilder = new StringBuilder();

        if (isNotEmpty(updateDefendantRepresentationRequestDefenceRepresentation.getOrganisationName())) {
            nameBuilder.append(updateDefendantRepresentationRequestDefenceRepresentation.getOrganisationName().trim().replaceAll(SPACE,UNDERSCORE));
        }

        final String defenceOrgName= nameBuilder.toString();

        Optional<ResponseDefinition> anyErrorResponse = getErrorCodeList().stream()
                .filter(error -> defenceOrgName.toUpperCase().contains(error))
                .map(error -> buildScenarioFailureResponse(error, error))
                .findFirst();

        if(StringUtils.containsIgnoreCase(defenceOrgName,"defence_org_llp_case_split_or_merged_ltd")) {
            return buildScenarioFailureResponse("404", "CASE_HAS_SPLIT_OR_MERGED");
        }

        if(StringUtils.containsIgnoreCase(defenceOrgName,"defence_org_llp_case_defence_rep_error")) {
            return buildScenarioFailureResponse("404", "DEFENCE_REPRESENTATION_ERROR");
        }
        return anyErrorResponse.orElse(buildSuccessResponse());

    }

    private ResponseDefinition buildSuccessResponse() {
        return new ResponseDefinitionBuilder()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withStatus(204)
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
        return DCS_DEFENCE_REPRESENTATION_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }


}
