package uk.gov.justice.services.jmx.system.command.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.system.command.client.connection.JMXConnectorFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import javax.management.remote.JMXConnector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TestSystemCommanderClientFactoryTest {

    @Mock
    private ObjectFactory objectFactory;

    @InjectMocks
    private TestSystemCommanderClientFactory testSystemCommanderClientFactory;

    @Test
    public void shouldCreateSystemCommanderClient() throws Exception {

        final JmxParameters jmxParameters = mock(JmxParameters.class);

        final MBeanConnector mBeanConnector = mock(MBeanConnector.class);
        final JMXConnector jmxConnector = mock(JMXConnector.class);
        final JMXConnectorFactory jmxConnectorFactory = mock(JMXConnectorFactory.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);

        when(objectFactory.mBeanConnector()).thenReturn(mBeanConnector);
        when(objectFactory.jmxConnectorFactory()).thenReturn(jmxConnectorFactory);
        when(jmxConnectorFactory.createJmxConnector(jmxParameters)).thenReturn(jmxConnector);
        when(objectFactory.systemCommanderClient(mBeanConnector, jmxConnector)).thenReturn(systemCommanderClient);

        assertThat(testSystemCommanderClientFactory.create(jmxParameters), is(systemCommanderClient));
    }
}
