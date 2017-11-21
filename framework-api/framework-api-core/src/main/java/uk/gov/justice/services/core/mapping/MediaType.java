package uk.gov.justice.services.core.mapping;

import static java.lang.String.format;

import java.util.Objects;

/**
 * Representation of a media type for usage when identifying schema ids in generated media type to
 * schema id mapping classes and requesting a schema for validating a json payload.
 */
public class MediaType {

    private final String type;
    private final String subtype;

    public MediaType(final String type, final String subtype) {
        this.type = type;
        this.subtype = subtype;
    }

    public MediaType(final String mediaType) {
        final int index = mediaType.indexOf('/');

        if (index < 0) {
            throw new MalformedMediaTypeException(format("Cannot parse media type '%s' into type and subtype. Missing slash character", mediaType));
        }

        type = mediaType.substring(0, index);
        subtype = mediaType.substring(index + 1);
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    @Override
    public String toString() {
        return type + "/" + subtype;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MediaType mediaType = (MediaType) o;
        return Objects.equals(type, mediaType.type) &&
                Objects.equals(subtype, mediaType.subtype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, subtype);
    }
}
