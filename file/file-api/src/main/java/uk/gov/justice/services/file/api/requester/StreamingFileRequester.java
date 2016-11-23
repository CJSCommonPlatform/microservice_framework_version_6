package uk.gov.justice.services.file.api.requester;

import java.io.InputStream;
import java.util.Optional;

public interface StreamingFileRequester{
    /**
     * Requests a file from the FileService as an InputStream.
     * @param fileId - the unique id of the file.
     * @param fileMimeType - mime-type of the file.
     * @param fileName - name of the file.
     * @return content of the file as InputStream.
     */
    Optional<InputStream> requestStreamed(final String fileId, final String fileMimeType, final String fileName);
}
