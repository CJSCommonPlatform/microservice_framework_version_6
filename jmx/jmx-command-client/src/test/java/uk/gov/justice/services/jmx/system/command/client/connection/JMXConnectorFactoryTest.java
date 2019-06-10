package uk.gov.justice.services.jmx.system.command.client.connection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.system.command.client.MBeanClientException;

import java.io.IOException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JMXConnectorFactoryTest {

    @Mock
    private JmxUrlFactory jmxUrlFactory;

    @Mock
    private ConnectorWrapper connectorWrapper;

    @InjectMocks
    private JMXConnectorFactory jmxConnectorFactory;

    @Test
    public void shouldCreateJMXConnectorWithTheCorrectNameAndPort() throws Exception {

        final String host = "localhost";
        final int port = 2384;

        final JMXServiceURL serviceURL = mock(JMXServiceURL.class);
        final JMXConnector jmxConnector = mock(JMXConnector.class);

        when(jmxUrlFactory.createUrl(host, port)).thenReturn(serviceURL);
        when(connectorWrapper.connect(serviceURL)).thenReturn(jmxConnector);

        assertThat(jmxConnectorFactory.createJMXConnector(host, port), is(jmxConnector));
    }

    @Test
    public void shouldThrowExceptionIfConnectionFails() throws Exception {

        final IOException ioException = new IOException("Ooops");

        final String host = "localhost";
        final int port = 2384;
        final String urlString = "service:jmx:remote+http://" + host + ":" + port;

        final JMXServiceURL serviceURL = new JMXServiceURL(urlString);

        when(jmxUrlFactory.createUrl(host, port)).thenReturn(serviceURL);
        when(connectorWrapper.connect(serviceURL)).thenThrow(ioException);

        try {
            jmxConnectorFactory.createJMXConnector(host, port);
            fail();
        } catch (final MBeanClientException expected) {
            assertThat(expected.getCause(), is(ioException));
            assertThat(expected.getMessage(), is("Failed to connect to JMX using url 'service:jmx:remote+http://localhost:2384'"));
        }
    }
}
