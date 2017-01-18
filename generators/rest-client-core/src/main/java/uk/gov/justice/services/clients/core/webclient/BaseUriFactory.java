package uk.gov.justice.services.clients.core.webclient;

import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.common.rest.DefaultServerPortProvider;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

@ApplicationScoped
@Default
public class BaseUriFactory {

    @Inject
    MockServerPortProvider mockServerPortProvider;

    @Inject
    DefaultServerPortProvider defaultServerPortProvider;


    public String createBaseUri(final EndpointDefinition definition) {
        return definition.getBaseUri().replace(":8080", ":" + getPort(definition));
    }

    private String getPort(final EndpointDefinition definition) {

        final Optional<String> mockServerPort = mockServerPortProvider.getMockServerPort(definition);

        if (mockServerPort.isPresent()) {
            return mockServerPort.get();
        }

        return defaultServerPortProvider.getDefaultPort();
    }
}
