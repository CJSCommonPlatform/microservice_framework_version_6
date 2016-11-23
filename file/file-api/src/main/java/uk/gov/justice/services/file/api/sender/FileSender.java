package uk.gov.justice.services.file.api.sender;

import java.io.InputStream;

/**
 * Interface for uploading file to a file service.
 */
public interface FileSender {

    FileData send(final String fileName, final InputStream content);

}
