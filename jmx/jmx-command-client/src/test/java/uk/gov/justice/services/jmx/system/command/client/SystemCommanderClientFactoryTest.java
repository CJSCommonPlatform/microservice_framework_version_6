package uk.gov.justice.services.jmx.system.command.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

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
public class SystemCommanderClientFactoryTest {

    @Mock
    private MBeanConnector mBeanConnector;

    @Mock
    private JMXConnectorFactory jmxConnectorFactory;

    @InjectMocks
    private SystemCommanderClientFactory systemCommanderClientFactory;

    @Test
    public void shouldCreateSystemCommanderClient() throws Exception {

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final JMXConnector jmxConnector = mock(JMXConnector.class);

        when(jmxConnectorFactory.createJmxConnector(jmxParameters)).thenReturn(jmxConnector);

        final SystemCommanderClient systemCommanderClient = systemCommanderClientFactory.create(jmxParameters);

        assertThat(getValueOfField(systemCommanderClient, "jmxConnector", JMXConnector.class), is(jmxConnector));
        assertThat(getValueOfField(systemCommanderClient, "mBeanConnector", MBeanConnector.class), is(mBeanConnector));
    }
}
