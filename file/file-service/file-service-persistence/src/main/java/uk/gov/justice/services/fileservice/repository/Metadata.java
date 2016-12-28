package uk.gov.justice.services.fileservice.repository;

import java.util.Objects;
import java.util.UUID;

import javax.json.JsonObject;

public class Metadata {

    private final UUID id;
    private final JsonObject json;
    private final  UUID fileId;

    public Metadata(final UUID id, final JsonObject json, final UUID fileId) {
        this.id = id;
        this.json = json;
        this.fileId = fileId;
    }

    public UUID getId() {
        return id;
    }

    public JsonObject getJson() {
        return json;
    }

    public UUID getFileId() {
        return fileId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Metadata metadata = (Metadata) o;
        return Objects.equals(getId(), metadata.getId()) &&
                Objects.equals(getJson(), metadata.getJson()) &&
                Objects.equals(getFileId(), metadata.getFileId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getJson(), getFileId());
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "id=" + id +
                ", json='" + json + '\'' +
                ", fileId=" + fileId +
                '}';
    }
}
