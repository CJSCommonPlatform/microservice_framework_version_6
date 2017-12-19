package uk.gov.justice.services.core.mapping;

import static java.util.Optional.ofNullable;

import java.util.Optional;

public class MediaTypes {

    private final MediaType requestMediaType;
    private final MediaType responseMediaType;

    public MediaTypes(final MediaType requestMediaType, final MediaType responseMediaType) {
        this.requestMediaType = requestMediaType;
        this.responseMediaType = responseMediaType;
    }

    public Optional<MediaType> getRequestMediaType() {
        return ofNullable(requestMediaType);
    }

    public Optional<MediaType> getResponseMediaType() {
        return ofNullable(responseMediaType);
    }
}
