package uk.gov.justice.services.file.api.sender;

import uk.gov.justice.services.file.api.FileData;

/**
 * Interface for uploading file to a file service.
 */
public interface FileSender {

    FileData send(final String fileName, final byte[] content);

}
