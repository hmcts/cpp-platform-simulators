package uk.gov.justice.cps.dto;

import java.io.InputStream;

public class RequestParts {
    private String materialId;
    private String metadataPayload;
    private InputStream materialInputStream;
    private String fileExtension;
    private String applicationId;

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(final String materialId) {
        this.materialId = materialId;
    }

    public String getMetadataPayload() {
        return metadataPayload;
    }

    public void setMetadataPayload(final String metadataPayload) {
        this.metadataPayload = metadataPayload;
    }

    public InputStream getMaterialInputStream() {
        return materialInputStream;
    }

    public void setMaterialInputStream(final InputStream materialInputStream) {
        this.materialInputStream = materialInputStream;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(final String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationId() { return this.applicationId; }
}
