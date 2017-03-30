package uk.gov.justice.services.core.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.Optional;

public interface ContextPayload {

    Optional<JsonEnvelope> getEnvelope();

    Optional<Object> getParameter(final String name);

    void setParameter(final String name, final Object parameter);

    Map<String, Object> copyOfParameters();
}
