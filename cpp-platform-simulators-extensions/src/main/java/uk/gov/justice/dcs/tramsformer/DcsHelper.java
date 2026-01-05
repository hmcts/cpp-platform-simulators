package uk.gov.justice.dcs.tramsformer;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.protocol.HTTP.CONTENT_TYPE;

import uk.gov.hmcts.dcs.openapi.model.ErrorResponsePayload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class DcsHelper {

    public static List<String> getErrorCodeList() {
        List<String> errorCodes = Arrays.stream(ErrorResponsePayload.ErrorCodeEnum.class.getEnumConstants())
                .map(ErrorResponsePayload.ErrorCodeEnum::getValue)
                .toList();
        List<String> finalErrorCodes = new ArrayList<>();
        finalErrorCodes.add("BAD_REQUEST");
        finalErrorCodes.add("PROBLEM");
        finalErrorCodes.add("INTERNAL_SERVER_ERROR");
        finalErrorCodes.addAll(errorCodes);
        return  finalErrorCodes;
    }

    public static ResponseDefinition buildScenarioFailureResponse(final String errorCode, final String errorMessage) {

        final ResponseDefinitionBuilder responseBuilder = new ResponseDefinitionBuilder()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON);
        final JsonObjectBuilder bodyBuilder = Json.createObjectBuilder();
        bodyBuilder.add("errorMessage", errorMessage.toUpperCase());

        if ("BAD_REQUEST".equalsIgnoreCase(errorCode)) {
            responseBuilder.withStatus(400);
        } else if("INTERNAL_SERVER_ERROR".equalsIgnoreCase(errorCode)){
            responseBuilder.withStatus(500);
        }else {
            responseBuilder.withStatus(404);
            bodyBuilder.add("errorCode", errorCode.toUpperCase());
        }

        return responseBuilder.withBody(bodyBuilder.build().toString()).build();
    }
}
