package uk.gov.justice.services.generators.commons.mapping;

import static java.util.Optional.ofNullable;

import uk.gov.justice.services.core.mapping.MediaType;

import java.util.Objects;
import java.util.Optional;

public class ActionNameMapping {

    private final String name;
    private final MediaType requestType;
    private final MediaType responseType;

    public ActionNameMapping(final String name, final MediaType requestType, final MediaType responseType) {
        this.name = name;
        this.requestType = requestType;
        this.responseType = responseType;
    }

    public String name() {
        return name;
    }

    Optional<MediaType> requestType() {
        return ofNullable(requestType);
    }

    Optional<MediaType> responseType() {
        return ofNullable(responseType);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ActionNameMapping that = (ActionNameMapping) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(requestType, that.requestType) &&
                Objects.equals(responseType, that.responseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, requestType, responseType);
    }
}
