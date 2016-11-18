package uk.gov.justice.services.file.api.requester;

import java.util.Optional;

/**
 * Interface for downloading file from file service.
 */
public interface FileRequester {

    Optional<byte[]> request(final String fileId, final String fileMimeType, final String fileName);

}
