package uk.gov.justice.services.adapter.rest.multipart;

import java.io.InputStream;

public class DefaultFileInputDetails implements FileInputDetails {

    private final String fileName;
    private final String fieldName;
    private final InputStream inputStream;

    public DefaultFileInputDetails(
            final String fileName,
            final String fieldName,
            final InputStream inputStream) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.inputStream = inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
