package uk.gov.justice.services.jmx.system.command.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.management.shuttering.commands.ShutterCommand.SHUTTER;
import static uk.gov.justice.services.management.shuttering.commands.UnshutterCommand.UNSHUTTER;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.jmx.api.mbean.SystemCommanderMBean;
import uk.gov.justice.services.jmx.system.command.client.connection.Credentials;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FrameworkSystemCommandCallerTest {

    @Mock
    private TestSystemCommanderClientFactory testSystemCommanderClientFactory;

    @Test
    public void shouldCallShutter() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final FrameworkSystemCommandCaller frameworkSystemCommandCaller = new FrameworkSystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        frameworkSystemCommandCaller.callShutter();

        verify(systemCommanderMBean).call(SHUTTER);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallUnshutter() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final FrameworkSystemCommandCaller frameworkSystemCommandCaller = new FrameworkSystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        frameworkSystemCommandCaller.callUnshutter();

        verify(systemCommanderMBean).call(UNSHUTTER);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCreateWithCorrectDefaultParametersIfInstantiatingUsingTheContextName() throws Exception {

        final String contextName = "contextName";
        final FrameworkSystemCommandCaller frameworkSystemCommandCaller = new FrameworkSystemCommandCaller(contextName);

        final JmxParameters jmxParameters = getValueOfField(frameworkSystemCommandCaller, "jmxParameters", JmxParameters.class);

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
