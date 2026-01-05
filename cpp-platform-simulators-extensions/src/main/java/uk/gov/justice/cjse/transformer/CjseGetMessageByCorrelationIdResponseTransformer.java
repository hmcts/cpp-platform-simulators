package uk.gov.justice.cjse.transformer;

import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.extractValue;
import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.getCorrelationId;
import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.getFileContentAsString;
import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.getLatestFileMatchingPredicate;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CjseGetMessageByCorrelationIdResponseTransformer extends ResponseDefinitionTransformer {

    final static Logger LOGGER = LoggerFactory.getLogger(CjseGetMessageByCorrelationIdResponseTransformer.class);
    public static final String CJSE_GET_MESSAGE_BY_CORRELATION_ID_RESPONSE_TRANSFORMER = "cjse-get-message-by-correlation-id-response-transformer";
    private static final Pattern PATTERN_TO_EXTRACT_CORRELATION_ID_FROM_URL = Pattern.compile("correlation_id=(.*)$");


    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition, final FileSource fileSource, final Parameters parameters) {
        long currentTime = System.currentTimeMillis();
        LOGGER.info("Request received to get message : {}", request.getUrl());
        String correlationId = extractCorrelationId(request.getUrl());
        ResponseDefinition response;
        try {
            response = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/xml")
                    .withStatus(200)
                    .withBody(getLatestFileMatchingCriteria(correlationId))
                    .build();


        } catch (RuntimeException rte) {
            LOGGER.error("Server exception {}", rte.getStackTrace());
            response = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/xml")
                    .withStatus(500)
                    .build();
        }
        long timeToExecute = System.currentTimeMillis() - currentTime;
        LOGGER.info("TIME TO EXECUTE: {}", timeToExecute);
        return response;
    }

    @Override
    public String getName() {
        return CJSE_GET_MESSAGE_BY_CORRELATION_ID_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private String getLatestFileMatchingCriteria(final String correlationId) {

        final Predicate<Path> predicateByCorrelationId = getPredicateByCorrelationId(correlationId);
        return getLatestFileMatchingPredicate(predicateByCorrelationId);
    }

    private Predicate<Path> getPredicateByCorrelationId(final String correlationId) {
        return path -> {
            final String fileContentAsString = getFileContentAsString(path.getFileName().toString());
            return getCorrelationId(fileContentAsString).equalsIgnoreCase(correlationId);
        };
    }

    private static String extractCorrelationId(final String url) {
        return extractValue(url, PATTERN_TO_EXTRACT_CORRELATION_ID_FROM_URL, 1);
    }

}
