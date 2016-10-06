package uk.gov.justice.services.clients.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.clients.core.DefaultServerPortProvider.DEFAULT_PORT;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DefaultServerPortProviderTest {


    @InjectMocks
    private DefaultServerPortProvider defaultServerPortProvider;

    @Before
    @After
    public void clearSystemProperty() {
        System.clearProperty(DEFAULT_PORT);
    }

    @Test
    public void shouldReturn8080IfDefaultPortNotSet() throws Exception {

        assertThat(System.getProperty(DEFAULT_PORT), is(nullValue()));
        assertThat(defaultServerPortProvider.getDefaultPort(), is("8080"));
    }

    @Test
    public void shouldReturnSystemPropertyDefaultPortIfSet() throws Exception {

        final String defaultPort = "827364";

        System.setProperty(DEFAULT_PORT, defaultPort);
        assertThat(defaultServerPortProvider.getDefaultPort(), is(defaultPort));
    }
}
