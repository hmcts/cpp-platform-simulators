package uk.gov.justice.twiff.transformer;

import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.extractValue;
import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.getFileContentAsString;
import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.getLatestFileMatchingPredicate;
import static uk.gov.justice.twiff.transformer.util.TwifTransformersUtil.getTransactionId;

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


public class GetMessageByTransactionIdResponseTransformer extends ResponseDefinitionTransformer {

    final static Logger LOGGER = LoggerFactory.getLogger(GetMessageByTransactionIdResponseTransformer.class);
    public static final String GET_MESSAGE_BY_TRANSACTION_ID_RESPONSE_TRANSFORMER = "get-message-by-transaction-id-response-transformer";
    private static final Pattern PATTERN_TO_EXTRACT_TRANSACTION_ID_FROM_URL = Pattern.compile("transaction_id=(.*)$");


    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition, final FileSource fileSource, final Parameters parameters) {
        long currentTime = System.currentTimeMillis();
        LOGGER.info("Request received to get message : {}", request.getUrl());
        String transactionId = extractTransactionId(request.getUrl());
        ResponseDefinition response;
        try {
            response = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/xml")
                    .withStatus(200)
                    .withBody(getLatestFileMatchingCriteria(transactionId))
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
        return GET_MESSAGE_BY_TRANSACTION_ID_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private String getLatestFileMatchingCriteria(final String transactionId) {

        final Predicate<Path> predicateByTransactionId = getPredicateByTransactionId(transactionId);
        return getLatestFileMatchingPredicate(predicateByTransactionId);
    }

    private Predicate<Path> getPredicateByTransactionId(final String transactionId) {
        return path -> {
            final String fileContentAsString = getFileContentAsString(path.getFileName().toString());
            return getTransactionId(fileContentAsString).equalsIgnoreCase(transactionId);
        };
    }

    private static String extractTransactionId(final String url) {
        return extractValue(url, PATTERN_TO_EXTRACT_TRANSACTION_ID_FROM_URL, 1);
    }

}
