package uk.gov.justice.services.file.api;

/**
 * Class to hold the file data for uploading or downloaded from a file service.
 */
public class FileData {

    private final String fileId;
    private final String fileMimeType;

    public FileData(final String fileId, final String fileMimeType) {
        this.fileId = fileId;
        this.fileMimeType = fileMimeType;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileMimeType() {
        return fileMimeType;
    }
}
