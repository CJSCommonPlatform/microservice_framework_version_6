package uk.gov.justice.services.metrics.interceptor;


import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MetricRegistryProducerTest {

    @Test
    public void shouldProduceRegistry() throws Exception {
        MetricRegistryProducer producer = new MetricRegistryProducer();
        assertThat(producer.metricRegistry(), not(nullValue()));
    }
}