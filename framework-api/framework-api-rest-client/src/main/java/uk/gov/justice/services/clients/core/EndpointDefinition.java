package uk.gov.justice.services.clients.core;

import java.util.Set;

/**
 * Class describing a REST endpoint.
 */
public class EndpointDefinition {

    private final String baseUri;
    private final String path;
    private final Set<String> pathParams;
    private final Set<QueryParam> queryParams;
    private final String responseMediaType;

    /**
     * Constructor.
     *
     * @param baseUri           the base URI for the endpoint
     * @param path              the path, with any path parameter names wrapped in curly brackets
     * @param pathParams        a set defining the path parameters to expect
     * @param queryParams       a set defining the query parameters this endpoint can take
     * @param responseMediaType the response media type of the endpoint
     */
    public EndpointDefinition(final String baseUri, final String path, final Set<String> pathParams,
                              final Set<QueryParam> queryParams, final String responseMediaType) {
        this.baseUri = baseUri;
        this.path = path;
        this.pathParams = pathParams;
        this.queryParams = queryParams;
        this.responseMediaType = responseMediaType;
    }

    /**
     * Get the base URI.
     *
     * @return the base URI
     */
    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Get the path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the path parameters as a set.
     *
     * @return the set of path parameters
     */
    public Set<String> getPathParams() {
        return pathParams;
    }

    /**
     * Get the query parameters as a set.
     *
     * @return the ste of query paremeters
     */
    public Set<QueryParam> getQueryParams() {
        return queryParams;
    }

    /**
     * Get the response media type for the request to next tier
     *
     * @return the media type as a String
     */
    public String getResponseMediaType() {
        return responseMediaType;
    }
}
