package uk.gov.justice.services.adapter.rest;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static javax.ws.rs.HttpMethod.GET;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;


public class BasicActionMapper {

    private static final String MEDIA_TYPE_PREFIX = "application/vnd.";
    private static final String MEDIA_TYPE_SEPARATOR = "/";

    private final Map<String, Map<String, String>> methodToMediaTypeAndActionMap = new HashMap<>();

    protected void add(final String methodName, final String mediaType, final String actionName) {
        methodToMediaTypeAndActionMap.computeIfAbsent(methodName, key -> new HashMap<>())
                .put(mediaType, actionName);
    }

    public String actionOf(final String methodName, final String httpMethod, final HttpHeaders headers) {
        return methodToMediaTypeAndActionMap.getOrDefault(methodName, emptyMap())
                .get(mediaTypeOf(httpMethod, headers));
    }

    private String mediaTypeOf(final String httpMethod, final HttpHeaders headers) {
        if (GET.equals(httpMethod)) {
            return headers.getAcceptableMediaTypes().stream()
                    .map(this::mediaType)
                    .filter(mt -> mt.startsWith(MEDIA_TYPE_PREFIX))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(format("No matching action for accept media types: %s", headers.getAcceptableMediaTypes())));
        } else {
            return mediaType(headers.getMediaType());
        }
    }

    private String mediaType(final MediaType mediaType) {
        return mediaType.getType() + MEDIA_TYPE_SEPARATOR + mediaType.getSubtype();
    }
}
