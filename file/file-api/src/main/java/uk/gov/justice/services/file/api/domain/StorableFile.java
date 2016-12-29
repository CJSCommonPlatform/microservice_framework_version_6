package uk.gov.justice.services.file.api.domain;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import javax.json.JsonObject;

public class StorableFile {

    private final UUID fileId;
    private final byte[] content;
    private final JsonObject metadata;

    public StorableFile(
            final UUID fileId,
            final JsonObject metadata,
            final byte[] content) {
        this.fileId = fileId;
        this.content = content;
        this.metadata = metadata;
    }

    public UUID getFileId() {
        return fileId;
    }

    public byte[] getContent() {
        return content;
    }

    public JsonObject getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StorableFile that = (StorableFile) o;
        return Objects.equals(getFileId(), that.getFileId()) &&
                Arrays.equals(getContent(), that.getContent()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileId(), getContent(), getMetadata());
    }

    @Override
    public String toString() {
        return "FileWithMetadata{" +
                "fileId=" + fileId +
                ", content=" + Arrays.toString(content) +
                ", metadata=" + metadata +
                '}';
    }
}
