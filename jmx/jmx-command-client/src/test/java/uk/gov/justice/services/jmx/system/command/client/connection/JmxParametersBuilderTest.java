package uk.gov.justice.services.jmx.system.command.client.connection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.jmx.system.command.client.connection.JmxParametersBuilder.jmxParameters;

import java.util.Optional;

import org.junit.Test;

public class JmxParametersBuilderTest {

    @Test
    public void shouldJmxParametersWithoutCredentials() throws Exception {

        final String host = "host";
        final int port = 8080;

        final JmxParameters jmxParameters = jmxParameters()
                .withHost(host)
                .withPort(port)
                .build();

        assertThat(jmxParameters.getHost(), is(host));
        assertThat(jmxParameters.getPort(), is(port));
        assertThat(jmxParameters.getCredentials().isPresent(), is(false));
    }

    @Test
    public void shouldJmxParametersWithCredentials() throws Exception {

        final String host = "host";
        final int port = 8080;
        final String username = "Fred";
        final String password = "Password123";

        final JmxParameters jmxParameters = jmxParameters()
                .withHost(host)
                .withPort(port)
                .withUsername(username)
                .withPassword(password)
                .build();

        assertThat(jmxParameters.getHost(), is(host));
        assertThat(jmxParameters.getPort(), is(port));

        final Optional<Credentials> credentials = jmxParameters.getCredentials();

        if(credentials.isPresent()) {
            assertThat(credentials.get().getUsername(), is(username));
            assertThat(credentials.get().getPassword(), is(password));
        } else {
            fail();
        }
    }
}
