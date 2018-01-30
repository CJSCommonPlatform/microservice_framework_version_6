package uk.gov.justice.services.adapter.direct;

import static java.lang.String.format;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultDirectAdapterProcessor implements DirectAdapterProcessor {


    @Override
    public JsonEnvelope process(final JsonEnvelope envelope,
                                final Function<InterceptorContext, Optional<JsonEnvelope>> interceptorContextFunction) {

        return interceptorContextFunction.apply(interceptorContextWithInput(envelope))
                .orElseThrow(() -> new IllegalStateException(format("Interceptor chain returned an empty envelope for: %s", envelope)));
    }

}
