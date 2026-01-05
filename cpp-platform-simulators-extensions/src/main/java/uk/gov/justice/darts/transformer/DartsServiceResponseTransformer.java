package uk.gov.justice.darts.transformer;

import static uk.gov.justice.darts.transformer.util.ResponseMessage.RESPONSE_200;
import static uk.gov.justice.util.FileUtil.getPayloadFromClassPath;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DartsServiceResponseTransformer extends ResponseDefinitionTransformer {

    final static Logger LOGGER = LoggerFactory.getLogger(DartsServiceResponseTransformer.class);
    public static final String DARTS_SERVICE_RESPONSE_TRANSFORMER = "darts-service-response-transformer";

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {
        LOGGER.info("Request received to get message : {}", request.getUrl());
        ResponseDefinition response;
        try {
            if (request.getMethod().equals(RequestMethod.GET)) {
                response = buildResponse(200, getPayloadFromClassPath("wsdl/DARTSService/DARTSService.wsdl"));
            }
            else {
                response = buildResponse(200, RESPONSE_200);
            }
        } catch (RuntimeException rte) {
            LOGGER.error("Server exception {}", rte.getStackTrace());
            response = buildResponse(500, rte.getMessage());
        }
        return response;
    }

    private ResponseDefinition buildResponse(int i, String response200) {
        ResponseDefinition response;
        response = new ResponseDefinitionBuilder()
                .withHeader("Content-Type", "text/xml")
                .withHeader("Set-Cookie", "JSESSIONID=dcba")
                .withStatus(i)
                .withBody(response200)
                .build();
        return response;
    }

    @Override
    public String getName() {
        return DARTS_SERVICE_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }
}
