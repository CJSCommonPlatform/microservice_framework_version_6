package uk.gov.justice.services.jmx;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.api.mbean.SystemCommander;
import uk.gov.justice.services.jmx.api.name.ObjectNameException;
import uk.gov.justice.services.jmx.api.name.ObjectNameFactory;

import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MBeanInstantiatorTest {

    @Mock
    private ObjectNameFactory objectNameFactory;

    @Mock
    private MBeanServer mbeanServer;

    @Mock
    private SystemCommander systemCommander;

    @InjectMocks
    private MBeanInstantiator mBeanInstantiator;

    @Test
    public void shouldRegisterSystemCommanderMBean() throws Exception {

        final ObjectName objectName = mock(ObjectName.class);

        when(mbeanServer.isRegistered(objectName)).thenReturn(false);
        when(objectNameFactory.create("systemCommander", "type", "SystemCommander")).thenReturn(objectName);

        mBeanInstantiator.registerSystemCommanderMBean();

        verify(mbeanServer).registerMBean(systemCommander, objectName);
    }

    @Test
    public void shouldNotRegisterSystemCommanderMBeanIfAlreadyRegistered() throws Exception {

        final ObjectName objectName = mock(ObjectName.class);

        when(objectNameFactory.create("systemCommander", "type", "SystemCommander")).thenReturn(objectName);
        when(mbeanServer.isRegistered(objectName)).thenReturn(true);

        mBeanInstantiator.registerSystemCommanderMBean();

        verify(mbeanServer, never()).registerMBean(systemCommander, objectName);
    }

    @Test
    public void shouldThrowExceptionWhenMBeanRegisteringIncorrect() throws Exception {

        final MBeanRegistrationException mBeanRegistrationException = new MBeanRegistrationException(new NullPointerException("Ooops"));
        final ObjectName objectName = mock(ObjectName.class, "AnObjectName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(false);
        when(objectNameFactory.create("systemCommander", "type", "SystemCommander")).thenReturn(objectName);
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

        final ObjectName objectName = mock(ObjectName.class);

        when(mbeanServer.isRegistered(objectName)).thenReturn(true);
        when(objectNameFactory.create("systemCommander", "type", "SystemCommander")).thenReturn(objectName);

        mBeanInstantiator.unregisterMBeans();

        verify(mbeanServer).unregisterMBean(objectName);
    }

    @Test
    public void shouldNotUnregisterMBeansIfNotAlreadyRegistered() throws Exception {

        final ObjectName objectName = mock(ObjectName.class);

        when(mbeanServer.isRegistered(objectName)).thenReturn(false);
        when(objectNameFactory.create("systemCommander", "type", "SystemCommander")).thenReturn(objectName);

        mBeanInstantiator.unregisterMBeans();

        verify(mbeanServer, never()).unregisterMBean(objectName);
    }

    @Test
    public void shouldThrowExceptionWhenMBeanUnregisteringIncorrect() throws Exception {
        final MBeanRegistrationException mBeanRegistrationException = new MBeanRegistrationException(new NullPointerException("Ooops"));

        final ObjectName objectName = mock(ObjectName.class, "AnObjectName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(true);
        when(objectNameFactory.create("systemCommander", "type", "SystemCommander")).thenReturn(objectName);
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
