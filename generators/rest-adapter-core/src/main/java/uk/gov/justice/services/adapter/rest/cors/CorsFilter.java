package uk.gov.justice.services.adapter.rest.cors;

import static javax.ws.rs.HttpMethod.OPTIONS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_MAX_AGE;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ORIGIN;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * JAX-RS filter for adding CORS headers for API layer service components.
 *
 * Copied from org.jboss.resteasy.plugins.interceptors.CorsFilter.
 */
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String CORS_FAILURE_PROPERTY_NAME = "cors.failure";

    private boolean allowCredentials = true;
    private String allowedMethods;
    private String allowedHeaders;
    private String exposedHeaders;
    private int corsMaxAge = -1;
    private Set<String> allowedOrigins = new HashSet<String>();

    /**
     * Put "*" if you want to accept all origins
     *
     * @return set of allowed origins
     */
    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * Defaults to true
     *
     * @return state of allowCredentials
     */
    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(final boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    /**
     * Will allow all by default
     *
     * @return allowed methods
     */
    public String getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * Will allow all by default comma delimited string for Access-Control-Allow-Methods
     *
     * @param allowedMethods comma delimited string of allowed methods
     */
    public void setAllowedMethods(final String allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public String getAllowedHeaders() {
        return allowedHeaders;
    }

    /**
     * Will allow all by default comma delimited string for Access-Control-Allow-Headers
     *
     * @param allowedHeaders comma delimited string of allow headers
     */
    public void setAllowedHeaders(final String allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public int getCorsMaxAge() {
        return corsMaxAge;
    }

    public void setCorsMaxAge(final int corsMaxAge) {
        this.corsMaxAge = corsMaxAge;
    }

    public String getExposedHeaders() {
        return exposedHeaders;
    }

    /**
     * comma delimited list
     *
     * @param exposedHeaders - comma delimited list of exposed headers
     */
    public void setExposedHeaders(final String exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String origin = requestContext.getHeaderString(ORIGIN);
        if (origin == null) {
            return;
        }
        if (requestContext.getMethod().equalsIgnoreCase(OPTIONS)) {
            preflight(origin, requestContext);
        } else {
            checkOrigin(requestContext, origin);
        }
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        final String origin = requestContext.getHeaderString(ORIGIN);
        if (origin == null || requestContext.getMethod().equalsIgnoreCase(OPTIONS) || requestContext.getProperty(CORS_FAILURE_PROPERTY_NAME) != null) {
            // don't do anything if origin is null, its an OPTIONS request, or cors.failure is set
            return;
        }
        responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        if (allowCredentials)
            responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

        if (exposedHeaders != null) {
            responseContext.getHeaders().putSingle(ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders);
        }
    }

    private void preflight(final String origin, final ContainerRequestContext requestContext) throws IOException {
        checkOrigin(requestContext, origin);

        final ResponseBuilder builder = Response.ok();
        builder.header(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        if (allowCredentials) builder.header(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        String requestMethods = requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_METHOD);
        if (requestMethods != null) {
            if (allowedMethods != null) {
                requestMethods = this.allowedMethods;
            }
            builder.header(ACCESS_CONTROL_ALLOW_METHODS, requestMethods);
        }
        String allowHeaders = requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_HEADERS);
        if (allowHeaders != null) {
            if (allowedHeaders != null) {
                allowHeaders = this.allowedHeaders;
            }
            builder.header(ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
        }
        if (corsMaxAge > -1) {
            builder.header(ACCESS_CONTROL_MAX_AGE, corsMaxAge);
        }
        requestContext.abortWith(builder.build());
    }

    private void checkOrigin(final ContainerRequestContext requestContext, String origin) {
        if (!allowedOrigins.contains("*") && !allowedOrigins.contains(origin)) {
            requestContext.setProperty(CORS_FAILURE_PROPERTY_NAME, true);
            throw new ForbiddenException("Origin not allowed: " + origin);
        }
    }
}
