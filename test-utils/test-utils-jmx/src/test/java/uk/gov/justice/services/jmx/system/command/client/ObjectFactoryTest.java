package uk.gov.justice.services.jmx.system.command.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import javax.management.remote.JMXConnector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ObjectFactoryTest {

    @InjectMocks
    private ObjectFactory objectFactory;

    @Test
    public void shouldCreateJmxUrlFactory() throws Exception {
        assertThat(objectFactory.jmxUrlFactory(), is(notNullValue()));
    }

    @Test
    public void shouldCreateConnectorWrapper() throws Exception {
        assertThat(objectFactory.connectorWrapper(), is(notNullValue()));
    }

    @Test
    public void shouldCreateJMXConnectorFactory() throws Exception {
        assertThat(objectFactory.jmxConnectorFactory(), is(notNullValue()));
    }

    @Test
    public void shouldCreateMBeanConnector() throws Exception {
        assertThat(objectFactory.mBeanConnector(), is(notNullValue()));
    }

    @Test
    public void shouldCreateRemoteMBeanFactory() throws Exception {
        assertThat(objectFactory.remoteMBeanFactory(), is(notNullValue()));
    }

    @Test
    public void shouldCreateObjectNameFactory() throws Exception {
        assertThat(objectFactory.objectNameFactory(), is(notNullValue()));
    }

    @Test
    public void shouldCreateSystemCommanderClient() throws Exception {

        final MBeanConnector mBeanConnector = mock(MBeanConnector.class);
        final JMXConnector jmxConnector = mock(JMXConnector.class);

        assertThat(objectFactory.systemCommanderClient(mBeanConnector, jmxConnector), is(notNullValue()));
    }

    @Test
    public void shouldCreateEnvironmentFactory() throws Exception {
        assertThat(objectFactory.environmentFactory(), is(notNullValue()));
    }
}
