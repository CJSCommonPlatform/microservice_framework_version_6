package uk.gov.justice.services.file.api;

import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FileData fileData = (FileData) o;
        return Objects.equals(getFileId(), fileData.getFileId()) &&
                Objects.equals(getFileMimeType(), fileData.getFileMimeType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileId(), getFileMimeType());
    }

    @Override
    public String toString() {
        return "FileData{" +
                "fileId='" + fileId + '\'' +
                ", fileMimeType='" + fileMimeType + '\'' +
                '}';
    }
}
