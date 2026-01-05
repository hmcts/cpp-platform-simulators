package uk.gov.justice.dcs.tramsformer;

import static java.util.Objects.nonNull;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.http.protocol.HTTP.CONTENT_TYPE;
import static uk.gov.justice.dcs.tramsformer.DcsHelper.buildScenarioFailureResponse;
import static uk.gov.justice.dcs.tramsformer.DcsHelper.getErrorCodeList;

import uk.gov.hmcts.dcs.openapi.model.Defendant;
import uk.gov.hmcts.dcs.openapi.model.DefendantOrganisation;
import uk.gov.hmcts.dcs.openapi.model.DefendantPerson;
import uk.gov.hmcts.dcs.openapi.model.UpdateDefendantDetailsRequest;

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

public class DcsDefendantUpdateResponseTransformer extends ResponseDefinitionTransformer {

    public static final Logger LOGGER = LoggerFactory.getLogger(DcsDefendantUpdateResponseTransformer.class);

    private static final String UNDERSCORE = "_";

    public static final String DCS_DEFENDANT_UPDATE_RESPONSE_TRANSFORMER = "dcs-defendant-update-response-transformer";

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {

        LOGGER.info("Transforming DCS defendant update response");
        LOGGER.info("Payload: {}", request.getBodyAsString());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            final UpdateDefendantDetailsRequest updateDefendantDetailsRequest = objectMapper.readValue(request.getBodyAsString(), UpdateDefendantDetailsRequest.class);
            return buildResponse(updateDefendantDetailsRequest);

        } catch (IOException e) {
            return buildFailureResponse();
        }

    }

    private ResponseDefinition buildResponse(final UpdateDefendantDetailsRequest request) {
        final Defendant defendant = request.getDefendant();
        StringBuilder nameBuilder = new StringBuilder();

        if (nonNull(defendant.getDefendantPerson())) {
            final DefendantPerson person = defendant.getDefendantPerson();

            if (isNotEmpty(person.getForename())) {
                nameBuilder.append(person.getForename().trim()).append(UNDERSCORE);
            }
            if (isNotEmpty(person.getMiddleName())) {
                nameBuilder.append(person.getMiddleName().trim()).append(UNDERSCORE);
            }
            nameBuilder.append(isNotEmpty(person.getSurname()) ? person.getSurname().trim() : person.getSurname()).toString();
        }
        if (nonNull(defendant.getDefendantOrganisation())) {
            final DefendantOrganisation organisation = defendant.getDefendantOrganisation();
            nameBuilder.append(organisation.getName().replaceAll(StringUtils.SPACE, UNDERSCORE));
        }

        final String defendantFinalName = nameBuilder.toString();

        Optional<ResponseDefinition> anyErrorResponse = getErrorCodeList().stream()
                .filter(error -> defendantFinalName.toUpperCase().contains(error))
                .map(error -> buildScenarioFailureResponse(error, error))
                .findFirst();
        if(StringUtils.containsIgnoreCase(defendantFinalName, "split_merged")) {
            return buildScenarioFailureResponse("404", "CASE_HAS_SPLIT_OR_MERGED");
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
        return DCS_DEFENDANT_UPDATE_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }


}
