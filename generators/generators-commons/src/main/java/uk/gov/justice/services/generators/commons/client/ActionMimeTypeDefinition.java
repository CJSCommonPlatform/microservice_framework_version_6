package uk.gov.justice.services.generators.commons.client;

import java.util.Optional;

import org.raml.model.MimeType;

/**
 * Action mime type definition combines the optional request and response type.  These define what
 * type of action this will map to in the framework.
 *
 * RequestType only             - asynchronous POST, PUT, PATCH or DELETE
 * ResponseType only            - synchronous GET
 * RequestType and ResponseType - synchronous POST, PUT or PATCH
 */
public class ActionMimeTypeDefinition {

    private static final Optional<MimeType> EMPTY_MIME_TYPE = Optional.empty();
    private static final String EXCEPTION_MESSAGE = "A RAML action must have either a request or response mimetype.";

    private final Optional<MimeType> requestType;
    private final Optional<MimeType> responseType;

    private ActionMimeTypeDefinition(final Optional<MimeType> requestType, final Optional<MimeType> responseType) {
        this.requestType = requestType;
        this.responseType = responseType;
    }

    public static ActionMimeTypeDefinition definitionWithResponse(final MimeType responseType) {
        if (null == responseType) {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }

        return new ActionMimeTypeDefinition(EMPTY_MIME_TYPE, Optional.of(responseType));
    }

    public static ActionMimeTypeDefinition definitionWithRequest(final MimeType requestType) {
        if (null == requestType) {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }

        return new ActionMimeTypeDefinition(Optional.of(requestType), EMPTY_MIME_TYPE);
    }

    public static ActionMimeTypeDefinition definitionWithRequestAndResponse(final MimeType requestType, final MimeType responseType) {
        if (null == requestType || null == responseType) {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }

        return new ActionMimeTypeDefinition(Optional.of(requestType), Optional.of(responseType));
    }

    public MimeType getResponseType() {
        return responseType.orElseGet(requestType::get);
    }

    public MimeType getNameType() {
        return requestType.orElseGet(responseType::get);
    }
}
