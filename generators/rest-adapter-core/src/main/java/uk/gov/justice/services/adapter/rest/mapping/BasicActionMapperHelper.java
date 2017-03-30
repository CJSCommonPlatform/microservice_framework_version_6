package uk.gov.justice.services.adapter.rest.mapping;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static javax.ws.rs.HttpMethod.GET;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;


public class BasicActionMapperHelper implements ActionMapperHelper {

    private static final String MEDIA_TYPE_SEPARATOR = "/";

    private final Map<String, Map<String, String>> methodToMediaTypeAndActionMap = new HashMap<>();

    @Override
    public void add(final String methodName, final String mediaType, final String actionName) {
        methodToMediaTypeAndActionMap.computeIfAbsent(methodName, key -> new HashMap<>())
                .put(mediaType, actionName);
    }

    @Override
    public String actionOf(final String methodName, final String httpMethod, final HttpHeaders headers) {
        final String action = methodToMediaTypeAndActionMap.getOrDefault(methodName, emptyMap())
                .get(mediaTypeOf(httpMethod, headers));
        if (action == null) {
            throw new BadRequestException(format("No matching action for accept media types: %s", headers.getAcceptableMediaTypes()));
        }
        return action;
    }

    private String mediaTypeOf(final String httpMethod, final HttpHeaders headers) {
        if (GET.equals(httpMethod)) {
            return headers.getAcceptableMediaTypes().stream()
                    .map(this::mediaType)
                    .findFirst()
                    .get();
        } else {
            return mediaType(headers.getMediaType());
        }
    }

    private String mediaType(final MediaType mediaType) {
        return mediaType.getType() + MEDIA_TYPE_SEPARATOR + mediaType.getSubtype();
    }
}
