package uk.gov.justice.cjse.transformer;

import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.extractValue;
import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.extractValueFromXML;
import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.getFileContentAsString;
import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.getLatestFileMatchingPredicate;
import static uk.gov.justice.cjse.transformer.util.CjseTransformersUtil.getUrn;

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

public class CjseGetMessageByUrnAndProsecutorReferenceResponseTransformer extends ResponseDefinitionTransformer {

    final static Logger LOGGER = LoggerFactory.getLogger(CjseGetMessageByUrnAndProsecutorReferenceResponseTransformer.class);
    public static final String CJSE_GET_MESSAGE_BY_URN_AND_PROSECUTOR_REFERENCE_RESPONSE_TRANSFORMER = "cjse-get-message-by-urn-and-prosecutor-reference-response-transformer";
    private static final Pattern PATTERN_TO_EXTRACT_PROSECUTOR_REFERENCE_FROM_XML = Pattern.compile("<(.*)ProsecutorReference>(.*?)<(.*)ProsecutorReference>");
    private static final Pattern PATTERN_FOR_URN = Pattern.compile("urn=(.*?)&");
    private static final Pattern PATTERN_FOR_PROSECUTOR_REFERENCE = Pattern.compile("prosecutor_reference=(.*?)$");

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {
        long currentTime = System.currentTimeMillis();
        LOGGER.info("Request received to get message : {}", request.getUrl());
        String urn = extractUrn(request.getUrl());
        String prosecutorReference = extractProsecutorReference(request.getUrl());
        ResponseDefinition response;
        try {
            response = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/xml")
                    .withStatus(200)
                    .withBody(getLatestFileMatchingCriteria(urn, prosecutorReference))
                    .build();


        } catch (RuntimeException rte) {
            LOGGER.error("Server exception {}", rte.getStackTrace());
            response = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/xml")
                    .withStatus(500)
                    .build();
        }
        long timeToExecute = System.currentTimeMillis() - currentTime;
        LOGGER.info("TIME TO EXECUTE : {}", timeToExecute);
        return response;
    }

    @Override
    public String getName() {
        return CJSE_GET_MESSAGE_BY_URN_AND_PROSECUTOR_REFERENCE_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private String getLatestFileMatchingCriteria(final String urn, final String prosecutorReference) {
        final Predicate<Path> predicateByUrnAndProsecutorReference = getPredicateByUrnAndProsecutorReference(urn, prosecutorReference);
        return getLatestFileMatchingPredicate(predicateByUrnAndProsecutorReference);
    }

    private Predicate<Path> getPredicateByUrnAndProsecutorReference(final String urn, final String prosecutorReference) {
        return path -> {
            final String fileContentAsString = getFileContentAsString(path.getFileName().toString());
            return getUrn(fileContentAsString).equalsIgnoreCase(urn) &&
                    getDefendantProsecutorReference(fileContentAsString).equalsIgnoreCase(prosecutorReference);
        };
    }

    private static String getDefendantProsecutorReference(final String fileContentAsString) {
        return extractValueFromXML(fileContentAsString, PATTERN_TO_EXTRACT_PROSECUTOR_REFERENCE_FROM_XML, 2);
    }

    private static String extractUrn(final String url) {
        return extractValue(url, PATTERN_FOR_URN, 1);
    }

    private static String extractProsecutorReference(final String url) {
        return extractValue(url, PATTERN_FOR_PROSECUTOR_REFERENCE, 1);
    }

}
