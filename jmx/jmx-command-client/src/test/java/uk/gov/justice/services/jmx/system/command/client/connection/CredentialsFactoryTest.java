package uk.gov.justice.services.jmx.system.command.client.connection;

import static javax.management.remote.JMXConnector.CREDENTIALS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CredentialsFactoryTest {

    @InjectMocks
    private CredentialsFactory credentialsFactory;

    @Test
    public void shouldCreateCorrectCredentials() throws Exception {

        final String username = "Fred";
        final String password = "Password123";

        final Map<String, Object> environment = credentialsFactory.create(username, password);

        assertThat(environment.size(), is(1));

        final String[] credentials = (String[]) environment.get(CREDENTIALS);

        assertThat(credentials.length, is(2));
        assertThat(credentials[0], is(username));
        assertThat(credentials[1], is(password));
    }
}
