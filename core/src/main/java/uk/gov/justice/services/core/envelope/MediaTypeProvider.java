package uk.gov.justice.services.core.envelope;

import static java.util.Optional.empty;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypes;
import uk.gov.justice.services.core.mapping.MediaTypesMappingCache;

import java.util.Optional;

import javax.inject.Inject;

public class MediaTypeProvider {

    private final MediaTypesMappingCache mediaTypesMappingCache;

    @Inject
    public MediaTypeProvider(final MediaTypesMappingCache mediaTypesMappingCache) {
        this.mediaTypesMappingCache = mediaTypesMappingCache;
    }

    public Optional<MediaType> getRequestMediaType(final String actionName) {
        final Optional<MediaTypes> mediaTypesOptional = mediaTypesMappingCache.mediaTypesFor(actionName);
        if (mediaTypesOptional.isPresent()) {
            return mediaTypesOptional.get().getRequestMediaType();
        }

        return empty();
    }

    public Optional<MediaType> getResponseMediaType(final String actionName) {
        final Optional<MediaTypes> mediaTypesOptional = mediaTypesMappingCache.mediaTypesFor(actionName);
        if (mediaTypesOptional.isPresent()) {
            return mediaTypesOptional.get().getResponseMediaType();
        }

        return empty();
    }
}
