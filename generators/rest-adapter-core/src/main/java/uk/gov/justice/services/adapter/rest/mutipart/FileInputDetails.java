package uk.gov.justice.services.adapter.rest.mutipart;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;

public class FileInputDetails {

    public static final String FILE_INPUT_DETAILS_LIST = "fileInputDetailsList";

    private final String fileName;
    private final String fieldName;
    private final InputStream inputStream;

    public FileInputDetails(
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
