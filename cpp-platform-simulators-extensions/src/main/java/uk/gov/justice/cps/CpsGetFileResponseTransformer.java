package uk.gov.justice.cps;

import static java.util.Base64.getEncoder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.util.UrlUtil.getFileNameFromUrl;

import uk.gov.justice.twiff.transformer.util.GlobalConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CpsGetFileResponseTransformer extends ResponseDefinitionTransformer {

    public static final String CPS_GET_FILE_RESPONSE_TRANSFORMER = "cps-get-file-response-transformer";
    private final static Logger LOGGER = LoggerFactory.getLogger(CpsGetFileResponseTransformer.class);

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {
        long currentTime = System.currentTimeMillis();
        LOGGER.info("Request received to get message : {}", request.getUrl());
        ResponseDefinition responseDefinition1;
        final String fileName = getFileNameFromUrl(request.getUrl(), "/cps/message-file");
        try {
            final String fileContent = getFileContentAsString(fileName);

            LOGGER.info("Sending response for the request {} : {}", request.getUrl(), fileContent);
            responseDefinition1 = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", getContentType(fileName))
                    .withStatus(200)
                    .withBody(fileContent)
                    .build();

        } catch (IOException e) {
            LOGGER.debug("File read is not successful", e);
            responseDefinition1 = new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(400)
                    .withBody(createObjectBuilder().add("error", "file not present").build().toString())
                    .build();
        }
        long timeToExecute = System.currentTimeMillis() - currentTime;
        LOGGER.info("Time to execute : {}", timeToExecute);
        return responseDefinition1;
    }

    @Override
    public String getName() {
        return CPS_GET_FILE_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private String getFileContentAsString(final String fileName) throws IOException {
        final byte[] contentAsByteArray = Files.readAllBytes(Paths.get(GlobalConstants.CPS_DIR + fileName));
        if (isTextPayload(fileName)) {
            return new String(contentAsByteArray);
        }

        return getEncoder().encodeToString(contentAsByteArray);


    }

    private String getContentType(final String filename) {
        return isTextPayload(filename) ? "application/txt" : "application/octet-stream";
    }

    private boolean isTextPayload(final String fileName) {
        return fileName.contains("payload");
    }

}
