package uk.gov.justice.services.jmx;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import java.lang.reflect.Field;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MBeanInstantiatorTest {

    @Spy
    private MBeanRegistry mBeanRegistry;

    @InjectMocks
    private MBeanInstantiator mBeanInstantiator;


    @Test
    public void shouldRegisterMBeans() throws Exception {

        mBeanInstantiator.registerMBeans();

        final MBeanServer mbeanServer = (MBeanServer) privateField("mbeanServer", mBeanInstantiator, MBeanInstantiator.class);

        final Set<ObjectInstance> instances = mbeanServer.queryMBeans(null, null);
        final boolean containsInstanceOfDefaultCatchupMBean = instances.stream().anyMatch(e -> e.getClassName().equals(DefaultCatchupMBean.class.getCanonicalName()));
        final boolean containsInstanceOfDefaultShutteringMBean = instances.stream().anyMatch(e -> e.getClassName().equals(DefaultShutteringMBean.class.getCanonicalName()));

        assertThat(containsInstanceOfDefaultCatchupMBean, is(true));
        assertThat(containsInstanceOfDefaultShutteringMBean, is(true));

        mBeanInstantiator.unregisterMBeans();
    }

    @Test
    public void shouldUnregisterMBeans() throws Exception {

        mBeanInstantiator.registerMBeans();
        mBeanInstantiator.unregisterMBeans();

        final MBeanServer mbeanServerWithUnregisteredMBeans = (MBeanServer) privateField("mbeanServer", mBeanInstantiator, MBeanInstantiator.class);

        final Set<ObjectInstance> instances1 = mbeanServerWithUnregisteredMBeans.queryMBeans(null, null);

        final boolean containsInstanceOfDefaultCatchupMBean1 = instances1.stream().anyMatch(e -> e.getClassName().equals(DefaultCatchupMBean.class.getCanonicalName()));
        final boolean containsInstanceOfDefaultShutteringMBean1 = instances1.stream().anyMatch(e -> e.getClassName().equals(DefaultShutteringMBean.class.getCanonicalName()));

        assertThat(containsInstanceOfDefaultCatchupMBean1, is(false));
        assertThat(containsInstanceOfDefaultShutteringMBean1, is(false));
    }

    @Test
    public void shouldThrowExceptionWhenMBeanRegisteringIncorrect() throws Exception {
        try {
            final MBeanServer mbeanServer = mock(MBeanServer.class);
            setField(mBeanInstantiator, "mbeanServer", mbeanServer);

            when(mbeanServer.registerMBean(any(), any())).thenThrow(new InstanceAlreadyExistsException("Not found"));
            mBeanInstantiator.registerMBeans();
            fail();
        } catch (final MBeanException expected) {
            assertThat(expected.getMessage(), is("Error during registration MXBean registration: Not found"));
        }
    }

    @Test
    public void shouldThrowExceptionWhenMBeanUnregisteringIncorrect() throws Exception {
        try {
            final MBeanServer mbeanServer = mock(MBeanServer.class);
            setField(mBeanInstantiator, "mbeanServer", mbeanServer);
            doThrow(new InstanceNotFoundException("Not found")).when(mbeanServer).unregisterMBean(any());
            mBeanInstantiator.unregisterMBeans();
            fail();
        } catch (final MBeanException expected) {
            assertThat(expected.getMessage(), is("Error during MXBean unregistration: Not found"));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T privateField(final String fieldName, final Object object, @SuppressWarnings("unused") final Class<T> clazz) throws Exception {
        final Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }
}