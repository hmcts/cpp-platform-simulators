package uk.gov.justice.cps;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.CPS_DIR;
import static uk.gov.justice.util.DateUtil.getFormattedDate;
import static uk.gov.justice.util.FileUtil.createFile;

import uk.gov.justice.cps.dto.RequestParts;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.json.JsonObject;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefenseMaterialNotificationSentToCpsResponseTransformer extends ResponseDefinitionTransformer {

    public static final Logger LOGGER = LoggerFactory.getLogger(DefenseMaterialNotificationSentToCpsResponseTransformer.class);

    public static final String DEFENSE_MATERIAL_NOTIFICATION_SENT_TO_CPS_RESPONSE_TRANSFORMER = "defense-material-notification-sent-to-cps-response-transformer";

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource fileSource, final Parameters parameters) {

        processRequestPayload(request);
        return buildResponse();
    }

    private ResponseDefinition buildResponse() {
        return new ResponseDefinitionBuilder()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(createObjectBuilder().add("notificationId", randomUUID().toString()).build().toString())
                .build();
    }

    private void processRequestPayload(final Request request) {
        final RequestParts requestParts = processMultiPartRequest(request);
        createFile(CPS_DIR, getFilename(requestParts.getMaterialId(), "payload", "txt"), requestParts.getMetadataPayload());
        createFile(CPS_DIR, getFilename(requestParts.getMaterialId(), "binary", requestParts.getFileExtension()), requestParts.getMaterialInputStream());
    }

    /**
     * Comment: Wiremock doesn't easily expose or support parsing multipart request.. this solution
     * was adapted from https://stackoverflow.com/a/42548549/973483 (not spending too much time on
     * this as this is only used for the simulator
     */
    private RequestParts processMultiPartRequest(final Request request) {
        try {
            ByteArrayDataSource datasource = new ByteArrayDataSource(request.getBody(), "multipart/form-data");
            MimeMultipart multipart = new MimeMultipart(datasource);

            int count = multipart.getCount();
            final RequestParts requestParts = new RequestParts();
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.getHeader("Content-Disposition")[0].contains("Notification")) {
                    final String content = IOUtils.toString(bodyPart.getInputStream(), StandardCharsets.UTF_8.name());
                    LOGGER.info("Json payload is  " + content);
                    requestParts.setMetadataPayload(content);
                    requestParts.setMaterialId(extractMaterialId(content));
                    requestParts.setFileExtension(extractFileExtension(content));
                } else if (bodyPart.getHeader("Content-Disposition")[0].contains("Material")) {
                    LOGGER.info("Handling file data ");
                    requestParts.setMaterialInputStream(bodyPart.getInputStream());
                }
            }
            return requestParts;

        } catch (final Exception e) {
            LOGGER.error("Unable to parse multipart request", e);
            throw new RuntimeException(e);
        }
    }

    private String extractFileExtension(final String jsonPayload) {
        final JsonObject payloadAsJsonObject = createReader(new StringReader(jsonPayload)).readObject();
        return getExtension(payloadAsJsonObject.getJsonObject("materialNotification").getString("fileName"));
    }

    private String extractMaterialId(final String jsonPayload) {
        final JsonObject payloadAsJsonObject = createReader(new StringReader(jsonPayload)).readObject();
        return payloadAsJsonObject.getJsonObject("materialNotification").getString("materialId");
    }


    @Override
    public String getName() {
        return DEFENSE_MATERIAL_NOTIFICATION_SENT_TO_CPS_RESPONSE_TRANSFORMER;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private String getFilename(final String materialId, final String suffix, final String extension) {
        return getFormattedDate() + "_" + materialId + "_" + suffix + "." + extension;
    }


}
