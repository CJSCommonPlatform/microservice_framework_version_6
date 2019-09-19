package uk.gov.justice.services.jmx.system.command.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.jmx.api.command.AddTriggerCommand;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.jmx.api.command.RemoveTriggerCommand;
import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterCommand;
import uk.gov.justice.services.jmx.api.command.ValidateCatchupCommand;
import uk.gov.justice.services.jmx.api.mbean.SystemCommanderMBean;
import uk.gov.justice.services.jmx.system.command.client.connection.Credentials;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    public void shouldCallIndexerCatchup() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final SystemCommandCaller systemCommandCaller = new SystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        systemCommandCaller.callIndexerCatchup();

        verify(systemCommanderMBean).call(new IndexerCatchupCommand());
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
    public void shouldCallAddTrigger() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final SystemCommandCaller systemCommandCaller = new SystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        systemCommandCaller.callAddTrigger();

        verify(systemCommanderMBean).call(new AddTriggerCommand());
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallRemoveTrigger() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final SystemCommandCaller systemCommandCaller = new SystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        systemCommandCaller.callRemoveTrigger();

        verify(systemCommanderMBean).call(new RemoveTriggerCommand());
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallValidateCatchup() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final SystemCommandCaller systemCommandCaller = new SystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        systemCommandCaller.callValidateCatchup();

        verify(systemCommanderMBean).call(new ValidateCatchupCommand());
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
