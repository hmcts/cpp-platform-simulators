package uk.gov.justice.dcs.tramsformer;

import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.http.protocol.HTTP.CONTENT_TYPE;
import static uk.gov.justice.dcs.tramsformer.DcsHelper.buildScenarioFailureResponse;

import uk.gov.hmcts.dcs.openapi.model.AddCaseMaterialRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DcsMaterialUpdateResponseTransformer extends ResponseDefinitionTransformer {

    public static final Logger LOGGER = LoggerFactory.getLogger(DcsMaterialUpdateResponseTransformer.class);

    public static final String DCS_MATERIAL_UPDATE_RESPONSE_TRANSFORMER = "dcs-material-update-response-transformer";
    private static final Map<String, String> sectionToErrorMap = new HashMap<>();

    static {
        sectionToErrorMap.put("Exhibits".toUpperCase(), "BAD_REQUEST");
        sectionToErrorMap.put("Notice".toUpperCase(), "INTERNAL_SERVER_ERROR");
        sectionToErrorMap.put("interviews".toUpperCase(), "INTERNAL_SERVER_ERROR");
        sectionToErrorMap.put("Witness Statements".toUpperCase(), "CASE_NOT_FOUND");
        sectionToErrorMap.put("Representation".toUpperCase(), "CASE_HAS_SPLIT_OR_MERGED");
        sectionToErrorMap.put("Sentence".toUpperCase(), "CASE_DELETED");
        sectionToErrorMap.put("Indictment".toUpperCase(), "DOCUMENT_NOT_UPLOADED");
        sectionToErrorMap.put("Key witness Statements".toUpperCase(), "DOCUMENT_CONTAINS_VIRUS");
        sectionToErrorMap.put("Case Summary".toUpperCase(), "MATERIAL_NOT_ACCEPTED");
        sectionToErrorMap.put("Trial Documents".toUpperCase(), "DEFENDANT_NOT_FOUND");
    }

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {

        LOGGER.info("Transforming DCS defendant update response");
        LOGGER.info("Payload: {}", request.getBodyAsString());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            final AddCaseMaterialRequest addCaseMaterialRequest = objectMapper.readValue(request.getBodyAsString(), AddCaseMaterialRequest.class);
            return buildResponse(addCaseMaterialRequest);

        } catch (IOException e) {
            return buildFailureResponse();
        }

    }

    private ResponseDefinition buildResponse(final AddCaseMaterialRequest request) {
        final String section = request.getDocumentSection();
        final String anyError = errorSectionName(section);

        if(!EMPTY.equalsIgnoreCase(anyError)){
            return buildScenarioFailureResponse(anyError, anyError);
        }

        return buildSuccessResponse();

    }

    private String errorSectionName(final String sectionName) {
        String error = EMPTY;
        String upperCaseSectionName = sectionName.toUpperCase();

        for (Map.Entry<String, String> entry : sectionToErrorMap.entrySet()) {
            if (upperCaseSectionName.contains(entry.getKey())) {
                error = entry.getValue();
                break;
            }
        }

        return error;
    }
    private ResponseDefinition buildSuccessResponse() {
        return new ResponseDefinitionBuilder()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withStatus(202)
                .build();
    }


    private ResponseDefinition buildFailureResponse() {
        return new ResponseDefinitionBuilder()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withStatus(400)
                .withBody(createObjectBuilder().add("errorMessage", "Unable to parse the payload").build().toString())
                .build();
    }

    @Override
    public String getName() {
        return DCS_MATERIAL_UPDATE_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }


}
