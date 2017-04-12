package uk.gov.justice.services.adapter.direct;

import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.ArrayUtils;

@ApplicationScoped
public class DefaultDirectAdapterProcessor implements DirectAdapterProcessor {

    private final String[] supportedActions;

    public DefaultDirectAdapterProcessor(final String[] supportedActions) {
        this.supportedActions = supportedActions;
    }

    @Override
    public JsonEnvelope process(final JsonEnvelope envelope,
                                final Function<InterceptorContext, Optional<JsonEnvelope>> interceptorContextFunction) {
        validate(envelope.metadata().name());

        return interceptorContextFunction.apply(interceptorContextWithInput(envelope))
                .orElseThrow(() -> new IllegalStateException(format("Interceptor chain returned an empty envelope for: %s", envelope)));
    }

    private void validate(final String actionName) {
        if (!contains(supportedActions, actionName)) {
            throw new IllegalStateException(format("Action name not supported: %s", actionName));
        }
    }
}