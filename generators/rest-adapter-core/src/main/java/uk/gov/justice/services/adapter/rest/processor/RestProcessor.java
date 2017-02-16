package uk.gov.justice.services.adapter.rest.processor;

import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public interface RestProcessor {

    /**
     * Process an incoming REST request by combining the payload, headers and path
     * parameters into an envelope and passing the envelope to the given consumer.
     *
     * @param responseStrategy the {@link ResponseStrategy} to use to process the response from the
     *                         interceptor chain
     * @param interceptorChain process envelope with this interceptor chain
     * @param action           the action name for this request
     * @param headers          the headers from the REST request
     * @param params           the parameters from the REST request
     * @return the HTTP response to return to the client
     */
    Response process(final ResponseStrategy responseStrategy,
                     final Function<JsonEnvelope, Optional<JsonEnvelope>> interceptorChain,
                     final String action,
                     final HttpHeaders headers,
                     final Collection<Parameter> params);

    /**
     * Process an incoming REST request by combining the payload, headers and path
     * parameters into an envelope and passing the envelope to the given consumer.
     *
     * @param responseStrategy the {@link ResponseStrategy} to use to process the response from the
     *                         interceptor chain
     * @param interceptorChain process envelope with this interceptor chain
     * @param action           the action name for this request
     * @param initialPayload   the payload from the REST request
     * @param headers          the headers from the REST request
     * @param params           the parameters from the REST request
     * @return the HTTP response to return to the client
     */
    Response process(final ResponseStrategy responseStrategy,
                     final Function<JsonEnvelope, Optional<JsonEnvelope>> interceptorChain,
                     final String action,
                     final Optional<JsonObject> initialPayload,
                     final HttpHeaders headers,
                     final Collection<Parameter> params);
}
