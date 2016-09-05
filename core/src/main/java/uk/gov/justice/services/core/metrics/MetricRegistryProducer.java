package uk.gov.justice.services.core.metrics;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

@ApplicationScoped
public class MetricRegistryProducer {

    @Produces
    public MetricRegistry metricRegistry() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        JmxReporter.forRegistry(metricRegistry).build().start();
        return metricRegistry;
    }
}
