package uk.gov.justice.services.fileservice.repository;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class File {

    private final UUID fileId;
    private final byte[] content;

    public File(final UUID fileId, final byte[] content) {
        this.fileId = fileId;
        this.content = content;
    }

    public UUID getFileId() {
        return fileId;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final File file = (File) o;
        return Objects.equals(getFileId(), file.getFileId()) &&
                Arrays.equals(getContent(), file.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileId(), getContent());
    }

    @Override
    public String toString() {
        return "File{" +
                "fileId=" + fileId +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
