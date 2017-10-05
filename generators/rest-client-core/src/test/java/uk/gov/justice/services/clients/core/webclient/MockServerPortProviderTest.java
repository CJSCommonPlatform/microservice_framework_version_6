package uk.gov.justice.services.clients.core.webclient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.clients.core.webclient.MockServerPortProvider.MOCK_SERVER_PORT;

import uk.gov.justice.services.clients.core.EndpointDefinition;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MockServerPortProviderTest {

    @Mock
    private ContextMatcher contextMatcher;

    @InjectMocks
    private MockServerPortProvider mockServerPortProvider;

    @Before @After
    public void clearSystemProperty() {
        System.clearProperty(MOCK_SERVER_PORT);
    }

    @Test
    public void shouldReturnMockServerPortIfSystemPropertySetAndIsNotTheSameService() throws Exception {

        final String port = "1234";
        System.setProperty(MOCK_SERVER_PORT, port);

        final EndpointDefinition endpointDefinition = mock(EndpointDefinition.class);

        when(contextMatcher.isSameContext(endpointDefinition)).thenReturn(false);

        final Optional<String> mockServerPort = mockServerPortProvider.getMockServerPort(endpointDefinition);

        assertThat(mockServerPort.isPresent(), is(true));
        assertThat(mockServerPort.get(), is(port));
    }

    @Test
    public void shouldReturnEmptyIfSystemPropertyIsSetButIsTheSameService() throws Exception {
        final String port = "1234";
        System.setProperty(MOCK_SERVER_PORT, port);

        final EndpointDefinition endpointDefinition = mock(EndpointDefinition.class);

        when(contextMatcher.isSameContext(endpointDefinition)).thenReturn(true);

        final Optional<String> mockServerPort = mockServerPortProvider.getMockServerPort(endpointDefinition);

        assertThat(mockServerPort.isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyIfSystemPropertyIsNotSet() throws Exception {

        assertThat(System.getProperty(MOCK_SERVER_PORT), is(nullValue()));

        final EndpointDefinition endpointDefinition = mock(EndpointDefinition.class);

        final Optional<String> mockServerPort = mockServerPortProvider.getMockServerPort(endpointDefinition);

        assertThat(mockServerPort.isPresent(), is(false));
    }
}
