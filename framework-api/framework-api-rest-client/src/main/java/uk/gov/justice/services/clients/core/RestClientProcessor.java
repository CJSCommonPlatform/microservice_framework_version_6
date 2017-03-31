package uk.gov.justice.services.clients.core;

import uk.gov.justice.services.messaging.JsonEnvelope;

public interface RestClientProcessor {

    /**
     * Make a synchronous GET request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     * @return the response that the endpoint returned for this request
     */
    JsonEnvelope get(final EndpointDefinition definition, final JsonEnvelope envelope);

    /**
     * Make an asynchronous POST request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     */
    void post(final EndpointDefinition definition, final JsonEnvelope envelope);

    /**
     * Make a synchronous POST request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     * @return the response that the endpoint returned for this request
     */
    JsonEnvelope synchronousPost(final EndpointDefinition definition, final JsonEnvelope envelope);

    /**
     * Make an asynchronous PUT request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     */
    void put(final EndpointDefinition definition, final JsonEnvelope envelope);

    /**
     * Make a synchronous PUT request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     * @return the response that the endpoint returned for this request
     */
    JsonEnvelope synchronousPut(final EndpointDefinition definition, final JsonEnvelope envelope);

    /**
     * Make an asynchronous PATCH request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     */
    void patch(final EndpointDefinition definition, final JsonEnvelope envelope);

    /**
     * Make a synchronous PATCH request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     * @return the response that the endpoint returned for this request
     */
    JsonEnvelope synchronousPatch(final EndpointDefinition definition, final JsonEnvelope envelope);

    /**
     * Make an asynchronous DELETE request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     */
    void delete(final EndpointDefinition definition, final JsonEnvelope envelope);
}