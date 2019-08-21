package uk.gov.justice.services.jmx.system.command.client.connection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.jmx.api.mbean.SystemCommanderMBean;
import uk.gov.justice.services.jmx.api.name.CommandMBeanNameProvider;
import uk.gov.justice.services.jmx.api.name.ObjectNameFactory;
import uk.gov.justice.services.jmx.system.command.client.MBeanClientException;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MBeanConnectorTest {

    @Mock
    private CommandMBeanNameProvider commandMBeanNameProvider;

    @Mock
    private RemoteMBeanFactory remoteMBeanFactory;

    @InjectMocks
    private MBeanConnector mBeanConnector;

    @Test
    public void shouldConnectToARemoteInstanceOfTheJmxBean() throws Exception {

        final String contextName = "my-context";
        final Class<SystemCommanderMBean> mBeanInterface = SystemCommanderMBean.class;

        final JMXConnector jmxConnector =  mock(JMXConnector.class);
        final ObjectName objectName = mock(ObjectName.class);
        final MBeanServerConnection connection = mock(MBeanServerConnection.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);
        when(jmxConnector.getMBeanServerConnection()).thenReturn(connection);
        when(connection.isRegistered(objectName)).thenReturn(true);
        when(remoteMBeanFactory.createRemote(connection, objectName, mBeanInterface)).thenReturn(systemCommanderMBean);

        assertThat(mBeanConnector.connect(contextName, mBeanInterface, jmxConnector), is(systemCommanderMBean));
    }

    @Test
    public void shouldThrowExceptionIfConnectingToTheMBeanFails() throws Exception {

        final IOException ioException = new IOException("Ooops");

        final String contextName = "my-context";
        final Class<SystemCommanderMBean> mBeanInterface = SystemCommanderMBean.class;

        final JMXConnector jmxConnector =  mock(JMXConnector.class);
        final ObjectName objectName = mock(ObjectName.class);

        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);
        when(jmxConnector.getMBeanServerConnection()).thenThrow(ioException);

        try {
            mBeanConnector.connect(contextName, mBeanInterface, jmxConnector);
            fail();
        } catch (final MBeanClientException expected) {
            assertThat(expected.getCause(), is(ioException));
            assertThat(expected.getMessage(), is("Failed to get remote connection to MBean 'SystemCommanderMBean'"));
        }
    }

    @Test
    public void shouldThrowExceptionIfMBeanNameNotCorrect() throws Exception {

        final String contextName = "people";
        final ObjectName realObjectName = createARealObjectName(contextName);

        final Class<SystemCommanderMBean> mBeanInterface = SystemCommanderMBean.class;
        final JMXConnector jmxConnector =  mock(JMXConnector.class);
        final MBeanServerConnection connection = mock(MBeanServerConnection.class);

        when(commandMBeanNameProvider.create(contextName)).thenReturn(realObjectName);
        when(jmxConnector.getMBeanServerConnection()).thenReturn(connection);
        when(connection.isRegistered(realObjectName)).thenReturn(false);


        try {
            mBeanConnector.connect(contextName, mBeanInterface, jmxConnector);
            fail();
        } catch (final MBeanClientException expected) {
            assertThat(expected.getMessage(), is("No JMX bean found with name 'people-system-command-handler-mbean'. Is your context name of 'people' correct?"));
        }
    }

    private ObjectName createARealObjectName(final String contextName) {
        final CommandMBeanNameProvider commandMBeanNameProvider = new CommandMBeanNameProvider();
        setField(commandMBeanNameProvider, "objectNameFactory", new ObjectNameFactory());

        return commandMBeanNameProvider.create(contextName);
    }
}
