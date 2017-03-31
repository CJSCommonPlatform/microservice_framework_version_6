package uk.gov.justice.services.adapter.rest.processor;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.multipart.FileBasedInterceptorContextFactory;
import uk.gov.justice.services.adapter.rest.multipart.FileInputDetails;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.HttpTraceLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

@ApplicationScoped
public class DefaultRestProcessor implements RestProcessor {

    @Inject
    Logger logger;

    @Inject
    RestEnvelopeBuilderFactory envelopeBuilderFactory;

    @Inject
    ResponseStrategyCache responseStrategyCache;

    @Inject
    FileBasedInterceptorContextFactory fileBasedInterceptorContextFactory;

    @Inject
    TraceLogger traceLogger;

    @Inject
    HttpTraceLoggerHelper httpTraceLoggerHelper;

    @Override
    public Response process(final String responseStrategyName,
                            final Function<InterceptorContext, Optional<JsonEnvelope>> interceptorChain,
                            final String action,
                            final HttpHeaders headers,
                            final Collection<Parameter> params) {
        return process(responseStrategyName, interceptorChain, action, empty(), headers, params, empty());
    }

    @Override
    public Response process(final String responseStrategyName,
                            final Function<InterceptorContext, Optional<JsonEnvelope>> interceptorChain,
                            final String action,
                            final Optional<JsonObject> initialPayload,
                            final HttpHeaders headers,
                            final Collection<Parameter> params) {

        return process(responseStrategyName, interceptorChain, action, initialPayload, headers, params, empty());
    }

    @Override
    public Response process(final String responseStrategyName,
                            final Function<InterceptorContext, Optional<JsonEnvelope>> interceptorChain,
                            final String action,
                            final HttpHeaders headers,
                            final Collection<Parameter> params,
                            final List<FileInputDetails> fileInputDetails) {

        return process(responseStrategyName, interceptorChain, action, empty(), headers, params, Optional.of(fileInputDetails));
    }

    private Response process(final String responseStrategyName,
                             final Function<InterceptorContext, Optional<JsonEnvelope>> interceptorChain,
                             final String action,
                             final Optional<JsonObject> initialPayload,
                             final HttpHeaders headers,
                             final Collection<Parameter> params,
                             final Optional<List<FileInputDetails>> fileInputDetails) {

        traceLogger.trace(logger, () -> format("Processing REST message: %s", httpTraceLoggerHelper.toHttpHeaderTrace(headers)));

        final JsonEnvelope envelope = envelopeBuilderFactory.builder()
                .withInitialPayload(initialPayload)
                .withAction(action)
                .withHeaders(headers)
                .withParams(params)
                .build();

        traceLogger.trace(logger, () -> format("REST message converted to envelope: %s", envelope));

        final InterceptorContext interceptorContext = fileInputDetails
                .map(value -> fileBasedInterceptorContextFactory.create(value, envelope))
                .orElseGet(() -> interceptorContextWithInput(envelope));

        final Optional<JsonEnvelope> result = interceptorChain.apply(interceptorContext);

        traceLogger.trace(logger, () -> format("REST message processed: %s", envelope));

        return responseStrategyCache.responseStrategyOf(responseStrategyName).responseFor(action, result);
    }
}