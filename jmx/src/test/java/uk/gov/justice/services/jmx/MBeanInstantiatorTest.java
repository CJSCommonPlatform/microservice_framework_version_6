package uk.gov.justice.services.jmx;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

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
    private MBeanRegistry mBeanRegistry;

    @Mock
    private MBeanServer mbeanServer;

    @InjectMocks
    private MBeanInstantiator mBeanInstantiator;

    @Test
    public void shouldRegisterMBeans() throws Exception {

        final Object mBean_1 = new Object();
        final Object mBean_2 = new Object();

        final ObjectName objectName_1 = mock(ObjectName.class);
        final ObjectName objectName_2 = mock(ObjectName.class);

        final Map<Object, ObjectName> mBeanMap = of(mBean_1, objectName_1, mBean_2, objectName_2);

        when(mBeanRegistry.getMBeanMap()).thenReturn(mBeanMap);

        mBeanInstantiator.registerMBeans();

        verify(mbeanServer).registerMBean(mBean_1, objectName_1);
        verify(mbeanServer).registerMBean(mBean_2, objectName_2);
    }

    @Test
    public void shouldUnregisterMBeans() throws Exception {

        final Object mBean_1 = new Object();
        final Object mBean_2 = new Object();

        final ObjectName objectName_1 = mock(ObjectName.class);
        final ObjectName objectName_2 = mock(ObjectName.class);

        final Map<Object, ObjectName> mBeanMap = of(mBean_1, objectName_1, mBean_2, objectName_2);

        when(mBeanRegistry.getMBeanMap()).thenReturn(mBeanMap);

        mBeanInstantiator.unregisterMBeans();

        verify(mbeanServer).unregisterMBean(objectName_1);
        verify(mbeanServer).unregisterMBean(objectName_2);
    }

    @Test
    public void shouldThrowExceptionWhenMBeanRegisteringIncorrect() throws Exception {

        final MBeanRegistrationException mBeanRegistrationException = new MBeanRegistrationException(new NullPointerException("Ooops"));

        final Object mBean_1 = "mBean_1";
        final Object mBean_2 = "mBean_2";

        final ObjectName objectName_1 = mock(ObjectName.class, "objectName_1");
        final ObjectName objectName_2 = mock(ObjectName.class, "objectName_2");

        final Map<Object, ObjectName> mBeanMap = of(mBean_1, objectName_1, mBean_2, objectName_2);

        when(mBeanRegistry.getMBeanMap()).thenReturn(mBeanMap);
        doThrow(mBeanRegistrationException).when(mbeanServer).registerMBean(mBean_2, objectName_2);

        try {
            mBeanInstantiator.registerMBeans();
            fail();
        } catch (final MBeanException expected) {
            assertThat(expected.getCause(), is(mBeanRegistrationException));
            assertThat(expected.getMessage(), is("MXBean registration failed for key 'mBean_2', value 'objectName_2'"));
        }
    }

    @Test
    public void shouldThrowExceptionWhenMBeanUnregisteringIncorrect() throws Exception {
        final MBeanRegistrationException mBeanRegistrationException = new MBeanRegistrationException(new NullPointerException("Ooops"));

        final Object mBean_1 = "mBean_1";
        final Object mBean_2 = "mBean_2";

        final ObjectName objectName_1 = mock(ObjectName.class, "objectName_1");
        final ObjectName objectName_2 = mock(ObjectName.class, "objectName_2");

        final Map<Object, ObjectName> mBeanMap = of(mBean_1, objectName_1, mBean_2, objectName_2);

        when(mBeanRegistry.getMBeanMap()).thenReturn(mBeanMap);
        doThrow(mBeanRegistrationException).when(mbeanServer).unregisterMBean(objectName_2);

        try {
            mBeanInstantiator.unregisterMBeans();
            fail();
        } catch (final MBeanException expected) {
            assertThat(expected.getCause(), is(mBeanRegistrationException));
            assertThat(expected.getMessage(), is("MXBean unregistration failed for key 'mBean_2', value 'objectName_2'"));
        }
    }
}
