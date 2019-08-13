package uk.gov.justice.services.jmx.system.command.client;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterCommand;
import uk.gov.justice.services.jmx.api.mbean.SystemCommanderMBean;
import uk.gov.justice.services.jmx.system.command.client.connection.Credentials;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class SystemCommandCallerTest {

    @Mock
    private TestSystemCommanderClientFactory testSystemCommanderClientFactory;

    @Test
    public void shouldCallRebuild() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final SystemCommandCaller systemCommandCaller = new SystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        systemCommandCaller.callRebuild();

        verify(systemCommanderMBean).call(new RebuildCommand());
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallCatchup() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final SystemCommandCaller systemCommandCaller = new SystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        systemCommandCaller.callCatchup();

        verify(systemCommanderMBean).call(new CatchupCommand());
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallShutter() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final SystemCommandCaller systemCommandCaller = new SystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        systemCommandCaller.callShutter();

        verify(systemCommanderMBean).call(new ShutterCommand());
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallUnshutter() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final SystemCommandCaller systemCommandCaller = new SystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        systemCommandCaller.callUnshutter();

        verify(systemCommanderMBean).call(new UnshutterCommand());
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCreateWithCorrectDefaultParametersIfInstantiatingUsingTheContextName() throws Exception {

        final String contextName = "contextName";
        final SystemCommandCaller systemCommandCaller = new SystemCommandCaller(contextName);

        final JmxParameters jmxParameters = getValueOfField(systemCommandCaller, "jmxParameters", JmxParameters.class);

        assertThat(jmxParameters.getContextName(), is(contextName));
        assertThat(jmxParameters.getHost(), is(getHost()));
        assertThat(jmxParameters.getPort(), is(9990));

        final Optional<Credentials> credentials = jmxParameters.getCredentials();

        if (credentials.isPresent()) {
            assertThat(credentials.get().getUsername(), is("admin"));
            assertThat(credentials.get().getPassword(), is("admin"));
        } else {
            fail();
        }
    }
}
