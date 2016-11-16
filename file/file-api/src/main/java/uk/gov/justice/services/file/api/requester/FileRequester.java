package uk.gov.justice.services.file.api.requester;

import uk.gov.justice.services.file.api.FileData;

import java.util.Optional;

/**
 * Interface for downloading file from file service.
 */
public interface FileRequester {

    Optional<byte[]> request(final FileData fileData, final String fileName);

}
