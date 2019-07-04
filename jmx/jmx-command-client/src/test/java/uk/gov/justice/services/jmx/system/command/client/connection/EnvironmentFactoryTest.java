package uk.gov.justice.services.jmx.system.command.client.connection;

import static javax.management.remote.JMXConnector.CREDENTIALS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.jmx.system.command.client.connection.JmxParametersBuilder.jmxParameters;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentFactoryTest {

    @InjectMocks
    private EnvironmentFactory environmentFactory;

    @Test
    public void shouldCreateEnvironmentWithCredentialsIfPresent() throws Exception {

        final String username = "Fred";
        final String password = "Password123";

        final JmxParameters jmxParameters = jmxParameters()
                .withHost("host")
                .withPort(928134)
                .withUsername(username)
                .withPassword(password)
                .build();

        final Map<String, Object> environment = environmentFactory.create(jmxParameters);

        assertThat(environment.size(), is(1));

        final String[] credentialsArray = (String[]) environment.get(CREDENTIALS);

        assertThat(credentialsArray.length, is(2));
        assertThat(credentialsArray[0], is(username));
        assertThat(credentialsArray[1], is(password));
    }

    @Test
    public void shouldCreateEmptyEnvironmentIfCredentialsNotPresent() throws Exception {

        final JmxParameters jmxParameters = jmxParameters()
                .withHost("host")
                .withPort(928134)
                .build();

        assertThat(jmxParameters.getCredentials().isPresent(), is(false));

        final Map<String, Object> environment = environmentFactory.create(jmxParameters);

        assertThat(environment.isEmpty(), is(true));
    }
}
