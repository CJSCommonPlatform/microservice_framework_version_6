package uk.gov.justice.services.core.metrics;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

import javax.inject.Inject;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public abstract class AbstractMetricsInterceptor implements Interceptor {
    @Inject
    MetricRegistry metricsRegistry;

    @Inject
    ServiceContextNameProvider serviceContextNameProvider;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        final Timer.Context time = metricsRegistry.timer(timerNameOf(interceptorContext)).time();
        try {
            return interceptorChain.processNext(interceptorContext);
        } finally {
            time.stop();
        }
    }

    protected String componentName() {
        return serviceContextNameProvider.getServiceContextName();
    }
    protected abstract String timerNameOf(final InterceptorContext interceptorContext);
}
