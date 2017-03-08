package uk.gov.justice.services.adapter.rest.processor;

import uk.gov.justice.services.adapter.rest.mutipart.FileInputDetails;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategy;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public interface RestProcessor {

    /**
     * Process an incoming REST request by combining headers and path
     * parameters into an envelope and passing the envelope to the given consumer.
     *
     * @param responseStrategyName name of a bean implementing {@link ResponseStrategy} to use to process the response from the
     *                         interceptor chain
     * @param interceptorChain process envelope with this interceptor chain
     * @param action           the action name for this request
     * @param headers          the headers from the REST request
     * @param params           the parameters from the REST request
     * @return the HTTP response to return to the client
     */
    Response process(final String responseStrategyName,
                     final Function<InterceptorContext, Optional<JsonEnvelope>> interceptorChain,
                     final String action,
                     final HttpHeaders headers,
                     final Collection<Parameter> params);

    /**
     * Process an incoming REST request by combining the payload, headers and path
     * parameters into an envelope and passing the envelope to the given consumer.
     *
     * @param responseStrategyName name of a bean implementing {@link ResponseStrategy} to use to process the response from the
     *                         interceptor chain
     * @param interceptorChain process envelope with this interceptor chain
     * @param action           the action name for this request
     * @param initialPayload   the payload from the REST request
     * @param headers          the headers from the REST request
     * @param params           the parameters from the REST request
     * @return the HTTP response to return to the client
     */
    Response process(final String responseStrategyName,
                     final Function<InterceptorContext, Optional<JsonEnvelope>> interceptorChain,
                     final String action,
                     final Optional<JsonObject> initialPayload,
                     final HttpHeaders headers,
                     final Collection<Parameter> params);

    /**
     * Process an incoming REST request by combining the multpart file stream, headers and path
     * parameters into an envelope and passing the envelope to the given consumer.
     *
     * @param responseStrategyName name of a bean implementing {@link ResponseStrategy} to use to process the response from the
     *                         interceptor chain
     * @param interceptorChain process envelope with this interceptor chain
     * @param action           the action name for this request
     * @param headers          the headers from the REST request
     * @param params           the parameters from the REST request
     * @param fileInputDetails list of file input details from the multipart input
     * @return the HTTP response to return to the client
     */
    Response process(final String responseStrategyName,
                     final Function<InterceptorContext, Optional<JsonEnvelope>> interceptorChain,
                     final String action,
                     final HttpHeaders headers,
                     final Collection<Parameter> params,
                     final List<FileInputDetails> fileInputDetails);
}
