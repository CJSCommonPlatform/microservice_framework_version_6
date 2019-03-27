package uk.gov.justice.services.jmx;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.management.ObjectName;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MBeanRegistryTest {

    @Mock
    private MBeanFactory mBeanFactory;

    @InjectMocks
    private MBeanRegistry mBeanRegistry;

    @Test
    public void shouldCreateMBeanMap() throws Exception {

        final Object shutteringMBean = new Object();
        final Object catchupMBean = new Object();

        final ObjectName shutteringObjectName = mock(ObjectName.class);
        final ObjectName catchupObjectName = mock(ObjectName.class);

        final ImmutableMap<Object, ObjectName> mBeanMap = of(
                shutteringMBean, shutteringObjectName,
                catchupMBean, catchupObjectName
        );

        when(mBeanFactory.createMBeans()).thenReturn(mBeanMap);

        assertThat(mBeanRegistry.getMBeanMap(), is(mBeanMap));
    }

    @Test
    public void shouldCreateMBeanMapOnlyOnce() throws Exception {

        final Object shutteringMBean = new Object();
        final Object catchupMBean = new Object();

        final ObjectName shutteringObjectName = mock(ObjectName.class);
        final ObjectName catchupObjectName = mock(ObjectName.class);

        final ImmutableMap<Object, ObjectName> mBeanMap = of(
                shutteringMBean, shutteringObjectName,
                catchupMBean, catchupObjectName
        );

        when(mBeanFactory.createMBeans()).thenReturn(mBeanMap);

        mBeanRegistry.getMBeanMap();
        mBeanRegistry.getMBeanMap();
        mBeanRegistry.getMBeanMap();
        mBeanRegistry.getMBeanMap();
        mBeanRegistry.getMBeanMap();
        mBeanRegistry.getMBeanMap();

        verify(mBeanFactory, times(1)).createMBeans();
    }
}
