package uk.gov.justice.cjse.transformer;

import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.getUrn;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.CJSE_DIR;
import static uk.gov.justice.util.DateUtil.getFormattedDate;
import static uk.gov.justice.util.FileUtil.createFile;

import uk.gov.justice.cjse.transformer.util.CjseTransformersUtil;
import uk.gov.justice.twiff.transformer.util.ResponseMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CjseMessageReceivedResponseTransformer extends ResponseDefinitionTransformer {

    final static Logger logger = LoggerFactory.getLogger(CjseMessageReceivedResponseTransformer.class);

    public static final String CJSE_MESSAGE_RECEIVED_RESPONSE_TRANSFORMER = "cjse-message-received-response-transformer";
    private static final String PATTERN_STRING_TO_EXTRACT_REQUEST_ID = "<(.*)RequestID>(.*?)<(.*)RequestID>";

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {
        long currentTime = System.currentTimeMillis();
        logger.info("Message received : {} ", request.getBodyAsString());
        final ResponseDefinition responseDefinitionSuccess;
        try {
            String requestId = getRequestId(request.getBodyAsString());
            final String filename = getFilename(getUrn(request.getBodyAsString()), requestId);
            createFile(CJSE_DIR, filename, request.getBodyAsString());
            String correlationId = CjseTransformersUtil.getCorrelationId(request.getBodyAsString());
            responseDefinitionSuccess = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/xml")
                    .withStatus(200)
                    .withBody(generateResponse(requestId,correlationId))
                    .build();
            long timeToExecute = System.currentTimeMillis() - currentTime;
            logger.info("uk.gov.justice.twiff.transformer.CjseMessageReceivedResponseTransformer:TIME TO EXECUTE : {}", timeToExecute);
        } catch (RuntimeException rte) {
             return new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/xml")
                    .withStatus(500)
                    .withBody(generateFailureResponse(rte))
                    .build();
        }
        return responseDefinitionSuccess;
    }

    private String getRequestId(final String bodyAsString) {
        Pattern pattern = Pattern.compile(PATTERN_STRING_TO_EXTRACT_REQUEST_ID);
        Matcher matcher = pattern.matcher(bodyAsString);
        String requestId = "";
        if (matcher.find()) {
            requestId = matcher.group(2);
        }
        if(requestId==null || requestId.isEmpty()) {
            throw new RuntimeException("RequestId should not be null or empty");
        }
        return requestId;
    }

    @Override
    public String getName() {

        return CJSE_MESSAGE_RECEIVED_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private String generateResponse(String requestId, final String correlationId) {

        String response = "";

        switch (correlationId) {
            case "ERROR_200":
                response = ResponseMessage.RESPONSE_200_ERROR;
                break;
            case "ERROR_201":
                response = ResponseMessage.RESPONSE_201_TEMPORARY_PROBLEM;
                break;
            case "ERROR_202":
                response = ResponseMessage.RESPONSE_202_SERVER_FAILURE;
                break;
            case "ERROR_300":
                response = ResponseMessage.RESPONSE_300_FATAL_ERROR;
                break;
            case "ERROR_304":
                response = ResponseMessage.RESPONSE_304_WRONG_SOURCE;
                break;
            case "ERROR_305":
                response = ResponseMessage.RESPONSE_305_WRONG_DESTINATION;
                break;
            case "ERROR_306":
                response = ResponseMessage.RESPONSE_306_INVALID_REQUESTID;
                break;
            case "ERROR_307":
                response = ResponseMessage.RESPONSE_307_MODE_ERROR;
                break;
            default:
                response = ResponseMessage.RESPONSE_SUCCESS.replace("DUMMY_REQUEST_ID",requestId);
        }
        logger.info("Sending response : {}", response);
        return response;
    }

    private String generateFailureResponse(RuntimeException rte) {

        final String response = ResponseMessage.RESPONSE_FAILURE.replace("DUMMY_FAILURE_MESSAGE",rte.getLocalizedMessage());
        logger.info("Sending response : {}", response);
        return response;
    }

    private String getFilename(final String urn, final String requestId) {
        return getFormattedDate() + "_" + urn + "_" + requestId + ".xml";
    }
}
