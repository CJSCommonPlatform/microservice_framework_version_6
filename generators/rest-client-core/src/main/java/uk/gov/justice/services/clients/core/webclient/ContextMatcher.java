package uk.gov.justice.services.clients.core.webclient;

import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.common.configuration.JndiBasedServiceContextNameProvider;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ContextMatcher {

    public static final int SERVICE_NAME_POSITION = 0;
    public static final int WEB_CONTEXT_POSITION = 3;

    @Inject
    JndiBasedServiceContextNameProvider contextNameProvider;

    public boolean isSameContext(final EndpointDefinition definition) {
        final String currentServiceName = extractServiceFromContext(contextNameProvider.getServiceContextName());
        final String remoteServiceName = extractServiceFromContext(extractContextFromUri(definition.getBaseUri()));

        return currentServiceName.equals(remoteServiceName);
    }

    private String extractServiceFromContext(final String contextName) {
        return contextName.split("-")[SERVICE_NAME_POSITION];
    }

    private String extractContextFromUri(final String baseUri) {
        return baseUri.split("/")[WEB_CONTEXT_POSITION];
    }

}
