package uk.gov.justice.services.file.api.sender;


public class FileData {
    private final String fileId;
    private final String fileMimeType;

    public FileData(final String fileId, final String fileMimeType) {
        this.fileId = fileId;
        this.fileMimeType = fileMimeType;
    }
}
