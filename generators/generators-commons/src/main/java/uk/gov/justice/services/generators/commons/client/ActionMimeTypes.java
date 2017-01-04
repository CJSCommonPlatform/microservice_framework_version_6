package uk.gov.justice.services.generators.commons.client;

import java.util.Optional;

import org.raml.model.MimeType;

public class ActionMimeTypes {

    private static final Optional<MimeType> EMPTY_MIME_TYPE = Optional.empty();
    private static final String EXCEPTION_MESSAGE = "A RAML action must have either a request or response mimetype.";

    private final Optional<MimeType> requestType;
    private final Optional<MimeType> responseType;

    public static ActionMimeTypes actionWithResponseOf(final MimeType responseType) {
        return new ActionMimeTypes(EMPTY_MIME_TYPE, Optional.of(responseType));
    }

    public static ActionMimeTypes actionWithRequestOf(final MimeType requestType) {
        return new ActionMimeTypes(Optional.of(requestType), EMPTY_MIME_TYPE);
    }

    public static ActionMimeTypes actionWithRequestAndResponseOf(final MimeType requestType, final MimeType responseType) {
        return new ActionMimeTypes(Optional.of(requestType), Optional.of(responseType));
    }

    private ActionMimeTypes(final Optional<MimeType> requestType, final Optional<MimeType> responseType) {
        this.requestType = requestType;
        this.responseType = responseType;
    }

    public MimeType getResponseType() {
        return responseType.orElseGet(() -> requestType.orElseThrow(() -> new IllegalStateException(EXCEPTION_MESSAGE)));
    }

    public MimeType getNameType() {
        return requestType.orElseGet(() -> responseType.orElseThrow(() -> new IllegalStateException(EXCEPTION_MESSAGE)));
    }
}
