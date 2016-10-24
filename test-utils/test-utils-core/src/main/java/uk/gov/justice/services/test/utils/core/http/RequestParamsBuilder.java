package uk.gov.justice.services.test.utils.core.http;

import static java.util.Collections.singletonList;

import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Builder for creating RequestParams. Expects a url and a media type. All other
 * parameters for polling a request endpoint are given as defaults.
 */
public class RequestParamsBuilder {

    private final String url;
    private final String mediaType;
    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    private RequestParamsBuilder(final String url, final String mediaType) {
        this.url = url;
        this.mediaType = mediaType;
    }

    /**
     * Convenience method for creating a new RequestParamsBuilder
     *
     * @param url       the url of the rest endpoint
     * @param mediaType the media type
     * @return a new RequestParamsBuilder
     */
    public static RequestParamsBuilder requestParams(final String url, final String mediaType) {
        return new RequestParamsBuilder(url, mediaType);
    }

    /**
     * Adds a header to the rest request.
     *
     * @param name  the name of the header
     * @param value the value of the header
     * @return this
     */
    public RequestParamsBuilder withHeader(final String name, final Object value) {
        headers.put(name, singletonList(value));
        return this;
    }

    /**
     * Adds a map of headers to the rest request.
     *
     * @param headers a map of header names to header values
     * @return this
     */
    public RequestParamsBuilder withHeaders(final Map<String, Object> headers) {
        this.headers.putAll(new MultivaluedHashMap<>(headers));
        return this;
    }

    /**
     * Builds RequestParams from the supplied values/defaults
     *
     * @return RequestParams
     */
    public RequestParams build() {
        return new RequestParams(
                url,
                mediaType,
                headers
        );
    }
}
