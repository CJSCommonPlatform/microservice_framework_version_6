package uk.gov.justice.services.generators.commons.mapping;

import uk.gov.justice.services.core.mapping.MediaType;

import java.util.Objects;

public class MediaTypeToSchemaId {

    private final MediaType mediaType;
    private final String schemaId;

    public MediaTypeToSchemaId(final MediaType mediaType, final String schemaId) {
        this.mediaType = mediaType;
        this.schemaId = schemaId;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getSchemaId() {
        return schemaId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MediaTypeToSchemaId that = (MediaTypeToSchemaId) o;
        return Objects.equals(mediaType, that.mediaType) &&
                Objects.equals(schemaId, that.schemaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaType, schemaId);
    }

    @Override
    public String toString() {
        return "MediaTypeToSchemaId{" +
                "mediaType=" + mediaType +
                ", schemaId='" + schemaId + '\'' +
                '}';
    }
}
