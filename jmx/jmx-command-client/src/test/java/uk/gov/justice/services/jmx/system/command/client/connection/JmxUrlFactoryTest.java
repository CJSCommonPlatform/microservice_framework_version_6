package uk.gov.justice.services.jmx.system.command.client.connection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.services.jmx.system.command.client.MBeanClientConnectionException;

import java.net.MalformedURLException;

import javax.management.remote.JMXServiceURL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmxUrlFactoryTest {

    @InjectMocks
    private JmxUrlFactory jmxUrlFactory;

    @Test
    public void shouldCreateACorrectlyFormedJmxUrl() throws Exception {

        final String host = "localhost";
        final int port = 9009;

        final JMXServiceURL jmxServiceURL = jmxUrlFactory.createUrl(host, port);

        assertThat(jmxServiceURL.toString(), is("service:jmx:remote+http://localhost:9009"));
    }

    @Test
    public void shouldThrowExceptionOfHostnameIsMalformed() throws Exception {

        final String dodglyHostName = "{}\\";
        final int port = 9009;

        try {
            jmxUrlFactory.createUrl(dodglyHostName, port);
            fail();
        } catch (final MBeanClientConnectionException expected) {
            assertThat(expected.getMessage(), is("Failed to create JMX service url using host '{}\\' and port 9009"));
            assertThat(expected.getCause(), is(instanceOf(MalformedURLException.class)));
        }
    }
}
