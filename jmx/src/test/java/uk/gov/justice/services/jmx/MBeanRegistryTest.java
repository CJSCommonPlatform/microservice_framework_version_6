package uk.gov.justice.services.jmx;

import org.junit.Test;

import javax.management.ObjectName;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MBeanRegistryTest {

    @Test
    public void shouldCreateMBeanMap() {

        final MBeanRegistry mBeanRegistry = new MBeanRegistry();
        final Map<Object, ObjectName> mBeanMap = mBeanRegistry.getMBeanMap();

        assertThat(mBeanMap.isEmpty(), is(false));
        assertThat(mBeanMap.size(), is(2));

        assertThat(mBeanMap.keySet().stream().filter(p -> p instanceof Shuttering).count(), is(1l));
        assertThat(mBeanMap.keySet().stream().filter(p -> p instanceof Catchup).count(), is(1l));

        assertThat(mBeanMap.values().stream().filter(objectName -> objectName.getDomain().equals("shuttering")).count(), is(1l));
        assertThat(mBeanMap.values().stream().filter(objectName -> objectName.getDomain().equals("catchup")).count(), is(1l));

    }

}