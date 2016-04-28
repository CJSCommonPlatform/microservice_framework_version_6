package uk.gov.justice.services.clients.core;

import java.util.Set;

/**
 * Class describing a REST endpoint.
 */
public class EndpointDefinition {

    private final String baseURi;
    private final String path;
    private final Set<String> pathParams;
    private final Set<QueryParam> queryParams;

    /**
     * Constructor.
     *
     * @param baseUri     the base URI for the endpoint
     * @param path        the path, with any path parameter names wrapped in curly brackets
     * @param pathParams  a set defining the path parameters to expect
     * @param queryParams a set defining the query parameters this endpoint can take
     */
    public EndpointDefinition(final String baseUri, final String path, final Set<String> pathParams,
                              final Set<QueryParam> queryParams) {
        this.baseURi = baseUri;
        this.path = path;
        this.pathParams = pathParams;
        this.queryParams = queryParams;
    }

    /**
     * Get the base URI.
     *
     * @return the base URI
     */
    public String getBaseURi() {
        return baseURi;
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
}
