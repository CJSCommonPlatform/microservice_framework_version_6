package uk.gov.justice.services.jmx.system.command.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.api.mbean.SystemCommanderMBean;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import javax.management.remote.JMXConnector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemCommanderClientTest {

    @Mock
    private JMXConnector jmxConnector;

    @Mock
    private MBeanConnector mBeanConnector;

    @InjectMocks
    private SystemCommanderClient systemCommanderClient;

    @Test
    public void shouldGetTheRemoteSystemCommanderMBean() throws Exception {

        final String contextName = "my-context";
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        when(mBeanConnector.connect(
                contextName,
                SystemCommanderMBean.class,
                jmxConnector)).thenReturn(systemCommanderMBean);

        assertThat(systemCommanderClient.getRemote(contextName), is(systemCommanderMBean));
    }

    @Test
    public void shouldCloseTheJmxConnectorOnTryWithResources() throws Exception {

        try(final SystemCommanderClient anotherSystemCommanderClient = new SystemCommanderClient(jmxConnector, mBeanConnector)) {
            assertThat(anotherSystemCommanderClient, is(notNullValue()));
        }

        verify(jmxConnector).close();
    }
}
