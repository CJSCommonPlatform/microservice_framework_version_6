package uk.gov.justice.services.core.metrics;

import static java.lang.String.format;

import uk.gov.justice.services.core.interceptor.InterceptorContext;

public class TotalActionMetricsInterceptor extends AbstractMetricsInterceptor {
    private static final int PRIORITY = 1;

    @Override
    protected String timerNameOf(final InterceptorContext interceptorContext) {
        return format("%s.action.total", componentName());
    }

    @Override
    public int priority() {
        return PRIORITY;
    }
}