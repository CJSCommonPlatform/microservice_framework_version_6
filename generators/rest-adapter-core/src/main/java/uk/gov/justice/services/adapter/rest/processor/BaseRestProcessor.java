package uk.gov.justice.services.adapter.rest.processor;

import static java.lang.String.format;
import static javax.json.JsonValue.NULL;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.status;
import static uk.gov.justice.services.messaging.logging.HttpMessageLoggerHelper.toHttpHeaderTrace;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

public abstract class BaseRestProcessor implements RestProcessor {

    private final RestEnvelopeBuilderFactory envelopeBuilderFactory;

    private final Logger logger;

    BaseRestProcessor(final RestEnvelopeBuilderFactory envelopeBuilderFactory, final Logger logger) {
        this.envelopeBuilderFactory = envelopeBuilderFactory;
        this.logger = logger;
    }

    public Response processAsynchronously(final Consumer<JsonEnvelope> consumer,
                                          final String action,
                                          final Optional<JsonObject> initialPayload,
                                          final HttpHeaders headers,
                                          final Collection<Parameter> params) {

        trace(logger, () -> format("Processing REST message: %s", toHttpHeaderTrace(headers)));

        final JsonEnvelope envelope = envelopeBuilderFactory.builder()
                .withInitialPayload(initialPayload)
                .withAction(action)
                .withHeaders(headers)
                .withParams(params)
                .build();

        trace(logger, () -> format("REST message converted to envelope: %s", envelope));

        consumer.accept(envelope);

        trace(logger, () -> format("REST message processed: %s", envelope));

        return status(ACCEPTED).build();
    }

    public Response processSynchronously(final Function<JsonEnvelope, Optional<JsonEnvelope>> function,
                                         final String action,
                                         final HttpHeaders headers,
                                         final Collection<Parameter> params) {

        return processSynchronously(function, action, Optional.empty(), headers, params);
    }

    public Response processSynchronously(final Function<JsonEnvelope, Optional<JsonEnvelope>> function,
                                         final String action,
                                         final Optional<JsonObject> initialPayload,
                                         final HttpHeaders headers,
                                         final Collection<Parameter> params) {

        trace(logger, () -> format("Processing REST message: %s", toHttpHeaderTrace(headers)));

        final JsonEnvelope envelope = envelopeBuilderFactory.builder()
                .withInitialPayload(initialPayload)
                .withHeaders(headers)
                .withParams(params)
                .withAction(action)
                .build();

        trace(logger, () -> format("REST message converted to envelope: %s", envelope));

        final Optional<JsonEnvelope> result = function.apply(envelope);

        trace(logger, () -> format("REST message processed: %s", envelope));
        trace(logger, () -> format("Responding to REST message with: %s", result));

        if (result.isPresent()) {
            final JsonEnvelope outputEnvelope = result.get();

            if (outputEnvelope.payload() == NULL) {
                return status(NOT_FOUND).build();
            } else {
                return okResponseFrom(outputEnvelope);
            }
        } else {
            logger.error(format("Dispatcher returned a null envelope for %s", envelope.metadata().name()));
            return status(INTERNAL_SERVER_ERROR).build();
        }
    }

    protected abstract Response okResponseFrom(final JsonEnvelope envelope);
}
