package uk.gov.justice.services.jmx;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.mbean.SystemCommander;
import uk.gov.justice.services.jmx.api.name.CommandMBeanNameProvider;
import uk.gov.justice.services.jmx.api.name.ObjectNameException;

import javax.inject.Inject;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class MBeanInstantiatorTest {

    @Mock
    private MBeanServer mbeanServer;

    @Mock
    private SystemCommander systemCommander;

    @Mock
    private ServiceContextNameProvider serviceContextNameProvider;

    @Mock
    private CommandMBeanNameProvider commandMBeanNameProvider;

    @Mock
    private Logger logger;

    @InjectMocks
    private MBeanInstantiator mBeanInstantiator;

    @Test
    public void shouldRegisterSystemCommanderMBean() throws Exception {

        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "mBeanName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(false);
        when(serviceContextNameProvider.getServiceContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);

        mBeanInstantiator.registerSystemCommanderMBean();

        verify(mbeanServer).registerMBean(systemCommander, objectName);
        verify(logger).info("Registering SystemCommander MBean using name 'mBeanName'");
    }

    @Test
    public void shouldNotRegisterSystemCommanderMBeanIfAlreadyRegistered() throws Exception {

        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "mBeanName");

        when(serviceContextNameProvider.getServiceContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);
        when(mbeanServer.isRegistered(objectName)).thenReturn(true);

        mBeanInstantiator.registerSystemCommanderMBean();

        verify(mbeanServer, never()).registerMBean(systemCommander, objectName);
        verifyZeroInteractions(logger);
    }

    @Test
    public void shouldThrowExceptionWhenMBeanRegisteringIncorrect() throws Exception {

        final MBeanRegistrationException mBeanRegistrationException = new MBeanRegistrationException(new NullPointerException("Ooops"));
        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "AnObjectName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(false);
        when(serviceContextNameProvider.getServiceContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);
        doThrow(mBeanRegistrationException).when(mbeanServer).registerMBean(systemCommander, objectName);

        try {
            mBeanInstantiator.registerSystemCommanderMBean();
            fail();
        } catch (final ObjectNameException expected) {
            assertThat(expected.getCause(), is(mBeanRegistrationException));
            assertThat(expected.getMessage(), is("Failed to register SystemCommander MBean using object name 'AnObjectName'"));
        }
    }

    @Test
    public void shouldUnregisterMBeans() throws Exception {

        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "mBeanName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(true);
        when(serviceContextNameProvider.getServiceContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);

        mBeanInstantiator.unregisterMBeans();

        verify(mbeanServer).unregisterMBean(objectName);
        verify(logger).info("Unregistering SystemCommander MBean using name 'mBeanName'");
    }

    @Test
    public void shouldNotUnregisterMBeansIfNotAlreadyRegistered() throws Exception {

        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "mBeanName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(false);
        when(serviceContextNameProvider.getServiceContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);

        mBeanInstantiator.unregisterMBeans();

        verify(mbeanServer, never()).unregisterMBean(objectName);
        verifyZeroInteractions(logger);
    }

    @Test
    public void shouldThrowExceptionWhenMBeanUnregisteringIncorrect() throws Exception {
        final MBeanRegistrationException mBeanRegistrationException = new MBeanRegistrationException(new NullPointerException("Ooops"));

        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "AnObjectName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(true);
        when(serviceContextNameProvider.getServiceContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);
        doThrow(mBeanRegistrationException).when(mbeanServer).unregisterMBean(objectName);

        try {
            mBeanInstantiator.unregisterMBeans();
            fail();
        } catch (final ObjectNameException expected) {
            assertThat(expected.getCause(), is(mBeanRegistrationException));
            assertThat(expected.getMessage(), is("Failed to unregister MBean with object name 'AnObjectName'"));
        }
    }
}
