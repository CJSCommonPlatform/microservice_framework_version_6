package uk.gov.justice.services.file.api.requester;

import java.io.InputStream;
import java.util.Optional;

/**
 * Interface for downloading file from file service.
 */
public interface FileRequester {

    /**
     * Requests a file from the FileService.
     * @param fileId - the unique id of the file.
     * @param fileMimeType - mime-type of the file.
     * @param fileName - name of the file.
     * @return streamed content of the file.
     */
    Optional<InputStream> request(final String fileId, final String fileMimeType, final String fileName);

    /**
     * Requests a file from the FileService.
     * @param fileId - the unique id of the file.
     * @param fileName - name of the file.
     * @param transformPdf - is pdf transformation of the file needed.
     * @return streamed content of the file.
     */
    Optional<InputStream> request(final String fileId, final String fileMimeType, final String fileName, final boolean transformPdf);

}

