package uk.gov.justice.services.jmx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.management.ObjectName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MBeanFactoryTest {

    @Mock
    private Shuttering shuttering;

    @Mock
    private Catchup catchup;

    @Mock
    private ObjectNameFactory objectNameFactory;

    @InjectMocks
    private MBeanFactory mBeanFactory;

    @Test
    public void shouldCreateMBeanMap() throws Exception {

        final ObjectName shutteringObjectName = mock(ObjectName.class);
        final ObjectName catchupObjectName = mock(ObjectName.class);

        when(objectNameFactory.create("shuttering", "type", "Shuttering")).thenReturn(shutteringObjectName);
        when(objectNameFactory.create("catchup", "type", "Catchup")).thenReturn(catchupObjectName);

        final Map<Object, ObjectName> mBeans = mBeanFactory.createMBeans();

        assertThat(mBeans.get(shuttering), is(shutteringObjectName));
        assertThat(mBeans.get(catchup), is(catchupObjectName));
    }
}
