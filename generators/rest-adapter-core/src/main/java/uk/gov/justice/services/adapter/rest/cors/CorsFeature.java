package uk.gov.justice.services.adapter.rest.cors;

import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;

import uk.gov.justice.services.core.configuration.Value;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * Wrapper for configuring and instantiating the CORS filter.
 */
@Provider
public class CorsFeature implements Feature {

    @Inject
    @Value(key = "corsAllowedMethods", defaultValue = "GET, POST, DELETE, PUT, OPTIONS")
    String allowedMethods;

    @Inject
    @Value(key = "corsAllowedOrigin", defaultValue = "*")
    String allowedOrigin;

    @Inject
    @Value(key = "corsAllowedHeaders", defaultValue = "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, " + USER_ID + ", " + SESSION_ID + ", " + CLIENT_CORRELATION_ID)
    String allowedHeaders;

    @Override
    public boolean configure(final FeatureContext context) {
        final CorsFilter corsFilter = new CorsFilter();
        corsFilter.setAllowedMethods(allowedMethods);
        corsFilter.getAllowedOrigins().add(allowedOrigin);
        corsFilter.setAllowedHeaders(allowedHeaders);
        context.register(corsFilter);
        return true;
    }
}
