package uk.gov.justice.services.jmx;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.management.MBeanServer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MBeanServerProducerTest {

    @InjectMocks
    private MBeanServerProducer mBeanServerProducer;

    @Test
    public void shouldCreateAnMBeanServer() throws Exception {

        final MBeanServer mBeanServer = mBeanServerProducer.mBeanServer();

        assertThat(mBeanServer, is(notNullValue()));
    }
}
