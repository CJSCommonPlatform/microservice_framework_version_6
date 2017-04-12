package uk.gov.justice.services.adapter.direct;

import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.function.Function;

public interface DirectAdapterProcessor {
    JsonEnvelope process(JsonEnvelope envelope, Function<InterceptorContext, Optional<JsonEnvelope>> interceptorChain);
}
