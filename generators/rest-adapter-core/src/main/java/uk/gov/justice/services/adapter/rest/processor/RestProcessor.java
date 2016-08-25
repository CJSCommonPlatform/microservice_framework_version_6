package uk.gov.justice.services.adapter.rest.processor;

import static java.lang.String.format;
import static javax.json.JsonValue.NULL;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.messaging.logging.HttpMessageLoggerHelper.toHttpHeaderTrace;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.inject.Alternative;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

/**
 * In order to minimise the amount of generated code in the JAX-RS implementation classes, this
 * service encapsulates all the logic for building an envelope from the REST request, passing it to
 * a consumer and building a suitable response. This allows testing of this logic independently from
 * the automated generation code.
 */
@Alternative
public class RestProcessor {

    private static final Logger LOGGER = getLogger(RestProcessor.class);

    private final RestEnvelopeBuilderFactory envelopeBuilderFactory;

    private final Function<JsonEnvelope, String> responseBodyGenerator;

    private final boolean sendMetadataIdInHeader;

    RestProcessor(final RestEnvelopeBuilderFactory envelopeBuilderFactory,
                  final Function<JsonEnvelope, String> responseBodyGenerator,
                  final boolean sendMetadataIdInHeader) {
        this.envelopeBuilderFactory = envelopeBuilderFactory;
        this.responseBodyGenerator = responseBodyGenerator;
        this.sendMetadataIdInHeader = sendMetadataIdInHeader;
    }

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
    public Response processAsynchronously(final Consumer<JsonEnvelope> consumer,
                                          final String action,
                                          final Optional<JsonObject> initialPayload,
                                          final HttpHeaders headers,
                                          final Collection<Parameter> params) {

        trace(LOGGER, () -> format("Processing REST message: %s", toHttpHeaderTrace(headers)));

        final JsonEnvelope envelope = envelopeBuilderFactory.builder()
                .withInitialPayload(initialPayload)
                .withAction(action)
                .withHeaders(headers)
                .withParams(params)
                .build();

        trace(LOGGER, () -> format("REST message converted to envelope: %s", envelope));

        consumer.accept(envelope);

        trace(LOGGER, () -> format("REST message processed: %s", envelope));

        return status(ACCEPTED).build();
    }

    /**
     * Process an incoming REST request synchronously by combining the payload, headers and path
     * parameters into an envelope and passing the envelope to the given consumer.
     *
     * @param action  the action name for this request
     * @param headers the headers from the REST request
     * @param params  the parameters from the REST request
     * @return the HTTP response to return to the client
     */
    public Response processSynchronously(final Function<JsonEnvelope, Optional<JsonEnvelope>> function,
                                         final String action,
                                         final HttpHeaders headers,
                                         final Collection<Parameter> params) {

        trace(LOGGER, () -> format("Processing REST message: %s", toHttpHeaderTrace(headers)));

        final JsonEnvelope envelope = envelopeBuilderFactory.builder()
                .withHeaders(headers)
                .withParams(params)
                .withAction(action)
                .build();

        trace(LOGGER, () -> format("REST message converted to envelope: %s", envelope));

        final Optional<JsonEnvelope> result = function.apply(envelope);

        trace(LOGGER, () -> format("REST message processed: %s", envelope));
        trace(LOGGER, () -> format("Responding to REST message with: %s", result));

        if (result.isPresent()) {
            final JsonEnvelope outputEnvelope = result.get();

            if (outputEnvelope.payload() == NULL) {
                return status(NOT_FOUND).build();
            } else {
                final Response.ResponseBuilder response = status(OK);

                if (sendMetadataIdInHeader) {
                    response.header(ID, outputEnvelope.metadata().id());
                }

                return response.entity(responseBodyGenerator.apply(outputEnvelope)).build();
            }
        } else {
            LOGGER.error(format("Dispatcher returned a null envelope for %s", envelope.metadata().name()));
            return status(INTERNAL_SERVER_ERROR).build();
        }
    }
}
