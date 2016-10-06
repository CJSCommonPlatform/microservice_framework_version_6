package uk.gov.justice.services.clients.core.webclient;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.clients.core.webclient.ContextMatcher;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MockServerPortProvider {

    static final String MOCK_SERVER_PORT = "mock.server.port";

    @Inject
    ContextMatcher contextMatcher;

    public Optional<String> getMockServerPort(EndpointDefinition endpointDefinition) {

        final Optional<String> mockServerPort = ofNullable(System.getProperty(MOCK_SERVER_PORT));

        if (mockServerPort.isPresent() && contextMatcher.isSameContext(endpointDefinition)) {
            return empty();
        }

        return mockServerPort;
    }
}
