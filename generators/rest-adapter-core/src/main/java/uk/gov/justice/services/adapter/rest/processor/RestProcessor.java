package uk.gov.justice.services.adapter.rest.processor;

import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public interface RestProcessor {

    /**
     * Process an incoming REST request asynchronously by combining the payload, headers and path
     * parameters into an envelope and passing the envelope to the given consumer.
     *
     * @param consumer       a consumer for the envelope
     * @param action         the action name for this request
     * @param initialPayload the payload from the REST request
     * @param headers        the headers from the REST request
     * @param params         the parameters from the REST request
     * @return the HTTP response to return to the client
     */
    Response processAsynchronously(final Consumer<JsonEnvelope> consumer,
                                   final String action,
                                   final Optional<JsonObject> initialPayload,
                                   final HttpHeaders headers,
                                   final Collection<Parameter> params);

    /**
     * Process an incoming REST request synchronously by combining the payload, headers and path
     * parameters into an envelope and passing the envelope to the given consumer.
     *
     * @param action  the action name for this request
     * @param headers the headers from the REST request
     * @param params  the parameters from the REST request
     * @return the HTTP response to return to the client
     */
    Response processSynchronously(final Function<JsonEnvelope, Optional<JsonEnvelope>> function,
                                  final String action,
                                  final HttpHeaders headers,
                                  final Collection<Parameter> params);

    /**
     * Process an incoming REST request synchronously by combining the payload, headers and path
     * parameters into an envelope and passing the envelope to the given consumer.
     *
     * @param action         the action name for this request
     * @param initialPayload the payload from the REST request
     * @param headers        the headers from the REST request
     * @param params         the parameters from the REST request
     * @return the HTTP response to return to the client
     */
    Response processSynchronously(final Function<JsonEnvelope, Optional<JsonEnvelope>> function,
                                  final String action,
                                  final Optional<JsonObject> initialPayload,
                                  final HttpHeaders headers,
                                  final Collection<Parameter> params);

}
