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

public class CjseGetMessageByUrnAndFamilyNameResponseTransformer extends ResponseDefinitionTransformer {

    final static Logger LOGGER = LoggerFactory.getLogger(CjseGetMessageByUrnAndFamilyNameResponseTransformer.class);
    public static final String CJSE_GET_MESSAGE_BY_URN_RESPONSE_TRANSFORMER = "cjse-get-message-by-urn-response-transformer";
    private static final Pattern PATTERN_TO_EXTRACT_FAMILY_NAME =
            Pattern.compile("PersonDefendant[\\s\\S]*PersonFamilyName>(.*)</(.*)PersonFamilyName[\\s\\S]*PersonDefendant");
    private static final Pattern PATTERN_FOR_URN = Pattern.compile("urn=(.*?)&");
    private static final Pattern PATTERN_FOR_FAMILY_NAME = Pattern.compile("family_name=(.*?)$");

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {
        long currentTime = System.currentTimeMillis();
        LOGGER.info("Request received to get message : {}", request.getUrl());
        String urn = extractUrn(request.getUrl());
        String familyName = extractFamilyName(request.getUrl());
        ResponseDefinition response;
        try {
            response = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/xml")
                    .withStatus(200)
                    .withBody(getLatestFileMatchingCriteria(urn, familyName))
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
        return CJSE_GET_MESSAGE_BY_URN_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private String getLatestFileMatchingCriteria(final String urn, final String familyName) {
        final Predicate<Path> predicateByUrnAndFamilyName = getPredicateByUrnAndFamilyName(urn, familyName);
        return getLatestFileMatchingPredicate(predicateByUrnAndFamilyName);
    }

    private Predicate<Path> getPredicateByUrnAndFamilyName(final String urn, final String familyName) {
        return path -> {
            final String fileContentAsString = getFileContentAsString(path.getFileName().toString());
            return getUrn(fileContentAsString).equalsIgnoreCase(urn) &&
                    getDefendantFamilyName(fileContentAsString).equalsIgnoreCase(familyName);
        };
    }

    private static String getDefendantFamilyName(final String fileContentAsString) {
        return extractValueFromXML(fileContentAsString, PATTERN_TO_EXTRACT_FAMILY_NAME, 1);
    }

    private static String extractUrn(final String url) {
        return extractValue(url, PATTERN_FOR_URN, 1);
    }

    private static String extractFamilyName(final String url) {
        return extractValue(url, PATTERN_FOR_FAMILY_NAME, 1);
    }

}
