package uk.gov.justice.services.clients.core.webclient;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.common.http.DefaultServerPortProvider;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class BaseUriFactoryTest {

    @Mock
    private MockServerPortProvider mockServerPortProvider;

    @Mock
    private DefaultServerPortProvider defaultServerPortProvider;

    @InjectMocks
    private BaseUriFactory baseUriFactory;

    @Test
    public void shouldCreateABaseUriWithADefaultPortIfNoMockServerPortIsSet() {

        final String defaultPort = "9090";
        final String baseUri = "http://localhost:8080/anyUrl";
        final String resultUri = "http://localhost:9090/anyUrl";

        final EndpointDefinition endpointDefinition = mock(EndpointDefinition.class);

        when(endpointDefinition.getBaseUri()).thenReturn(baseUri);
        when(mockServerPortProvider.getMockServerPort(endpointDefinition)).thenReturn(empty());
        when(defaultServerPortProvider.getDefaultPort()).thenReturn(defaultPort);

        assertThat(baseUriFactory.createBaseUri(endpointDefinition), is(resultUri));
    }

    @Test
    public void shouldCreateABaseUriWithTheMockServerPortIfSet() {

        final Optional<String> mockServerPort = of("8989");
        final String baseUri = "http://localhost:8080/anyUrl";
        final String resultUri = "http://localhost:8989/anyUrl";

        final EndpointDefinition endpointDefinition = mock(EndpointDefinition.class);

        when(endpointDefinition.getBaseUri()).thenReturn(baseUri);
        when(mockServerPortProvider.getMockServerPort(endpointDefinition)).thenReturn(mockServerPort);

        assertThat(baseUriFactory.createBaseUri(endpointDefinition), is(resultUri));
    }
}
