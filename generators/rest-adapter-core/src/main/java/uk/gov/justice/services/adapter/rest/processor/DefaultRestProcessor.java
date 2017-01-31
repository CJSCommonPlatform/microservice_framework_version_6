package uk.gov.justice.services.adapter.rest.processor;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.status;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.messaging.logging.HttpMessageLoggerHelper.toHttpHeaderTrace;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

public class DefaultRestProcessor implements RestProcessor {

    private static final Logger LOGGER = getLogger(DefaultRestProcessor.class);

    private final RestEnvelopeBuilderFactory envelopeBuilderFactory;
    private final ResponseFactory responseFactory;

    DefaultRestProcessor(final RestEnvelopeBuilderFactory envelopeBuilderFactory,
                         final ResponseFactory responseFactory) {
        this.envelopeBuilderFactory = envelopeBuilderFactory;
        this.responseFactory = responseFactory;
    }

    @Override
    public Response processAsynchronously(final Function<JsonEnvelope, Optional<JsonEnvelope>> interceptorChain,
                                          final String action,
                                          final Optional<JsonObject> initialPayload,
                                          final HttpHeaders headers,
                                          final Collection<Parameter> params) {
        processInterceptorChain(interceptorChain, action, initialPayload, headers, params);
        return status(ACCEPTED).build();
    }

    @Override
    public Response processSynchronously(final Function<JsonEnvelope, Optional<JsonEnvelope>> interceptorChain,
                                         final String action,
                                         final HttpHeaders headers,
                                         final Collection<Parameter> params) {
        return processSynchronously(interceptorChain, action, Optional.empty(), headers, params);
    }

    @Override
    public Response processSynchronously(final Function<JsonEnvelope, Optional<JsonEnvelope>> interceptorChain,
                                         final String action,
                                         final Optional<JsonObject> initialPayload,
                                         final HttpHeaders headers,
                                         final Collection<Parameter> params) {
        final Optional<JsonEnvelope> result = processInterceptorChain(interceptorChain, action, initialPayload, headers, params);
        return responseFactory.responseFor(action, result);
    }

    private Optional<JsonEnvelope> processInterceptorChain(final Function<JsonEnvelope, Optional<JsonEnvelope>> interceptorChain,
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

        final Optional<JsonEnvelope> result = interceptorChain.apply(envelope);

        trace(LOGGER, () -> format("REST message processed: %s", envelope));

        return result;
    }
}