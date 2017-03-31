package uk.gov.justice.services.adapter.rest.multipart;

import java.io.InputStream;

public interface FileInputDetails {

    String FILE_INPUT_DETAILS_LIST = "fileInputDetailsList";

    String getFileName();

    String getFieldName();

    InputStream getInputStream();
}