package uk.gov.justice.services.core.metrics;

import uk.gov.justice.services.core.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

import javax.inject.Inject;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class MetricsInterceptor implements Interceptor {
    private static final int PRIORITY = 1;

    @Inject
    MetricRegistry metricsRegistry;

    @Inject
    ServiceContextNameProvider serviceContextNameProvider;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        final Timer.Context time = timer().time();
        try {
            return interceptorChain.processNext(interceptorContext);
        } finally {
            time.stop();
        }
    }

    private Timer timer() {
        return metricsRegistry.timer(serviceContextNameProvider.getServiceContextName());
    }

    @Override
    public int priority() {
        return PRIORITY;
    }
}
