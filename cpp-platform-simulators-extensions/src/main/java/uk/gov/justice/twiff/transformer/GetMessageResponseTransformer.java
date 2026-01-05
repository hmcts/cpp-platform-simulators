package uk.gov.justice.twiff.transformer;

import static uk.gov.justice.util.UrlUtil.getFileNameFromUrl;

import uk.gov.justice.twiff.transformer.util.GlobalConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

public class GetMessageResponseTransformer extends ResponseDefinitionTransformer {

    public static final String GET_FILE_RESPONSE_TRANSFORMER = "get-file-response-transformer";
    private final static Logger LOGGER = LoggerFactory.getLogger(GetMessageResponseTransformer.class);

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {
        long currentTime = System.currentTimeMillis();
        LOGGER.info("Request received to get message : {}", request.getUrl());
        ResponseDefinition responseDefinition1;
        String fileName = getFileNameFromUrl(request.getUrl(), "/message-file");
        try {
            String fileContent = getFileContentAsString(fileName);
            LOGGER.info("Sending response for the request {} : {}", request.getUrl(), fileContent);
            responseDefinition1 = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/xml")
                    .withStatus(200)
                    .withBody(fileContent)
                    .build();

        } catch (IOException e) {
            LOGGER.debug("File read is not successful", e);
            responseDefinition1 = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/xml")
                    .withStatus(400)
                    .withBody("FileNotPresent")
                    .build();
        }
        long timeToExecute = System.currentTimeMillis() - currentTime;
        LOGGER.info("uk.gov.justice.twiff.transformer.GetMessageResponseTransformer:TIME TO EXECUTE : {}", timeToExecute);
        return responseDefinition1;
    }

    @Override
    public String getName() {
        return GET_FILE_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private static String getFileContentAsString(final String fileName) throws IOException {

        String fileContentWithSOAPEnvelope = new String(Files.readAllBytes(Paths.get(GlobalConstants.TWIF_DIR + fileName)));
        return getContentOfMessageTag(fileContentWithSOAPEnvelope);
    }

    private static String getContentOfMessageTag(final String fileContentWithSOAPEnvelope) {

        String pattern3 = "<Message><!\\[CDATA\\[(.*?)\\]\\]></Message>";
        Pattern pattern = Pattern.compile(pattern3);
        Matcher matcher = pattern.matcher(fileContentWithSOAPEnvelope);
        String messageName = "";
        if (matcher.find()) {
            messageName = matcher.group(1);
        }
        return messageName;
    }

}
