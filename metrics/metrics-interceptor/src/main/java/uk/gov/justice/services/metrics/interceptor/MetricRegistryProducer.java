package uk.gov.justice.services.metrics.interceptor;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

@ApplicationScoped
public class MetricRegistryProducer {

    @Produces
    public MetricRegistry metricRegistry() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        JmxReporter.forRegistry(metricRegistry).inDomain("uk.gov.justice.metrics").build().start();
        return metricRegistry;
    }
}
