package uk.gov.justice.services.adapter.messaging;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.common.log.LoggerConstants.METADATA;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;
import static uk.gov.justice.services.common.log.LoggerConstants.SERVICE_CONTEXT;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import java.util.Optional;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.TextMessage;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Interceptor gets the Metadata from the payload and adds metadata information to the Logger Mapped
 * Diagnostic Context.  This can be added to the log output by setting %X{frameworkRequestData} in
 * the logger pattern.
 */
public class JmsLoggerMetadataInterceptor {

    @Inject
    Logger logger;

    @Inject
    JmsParameterChecker parameterChecker;

    @Inject
    ServiceContextNameProvider serviceContextNameProvider;

    @Inject
    JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Inject
    TraceLogger traceLogger;

    @AroundInvoke
    protected Object addRequestDataToMappedDiagnosticContext(final InvocationContext context) throws Exception {
        traceLogger.trace(logger, () -> "Adding Request data to MDC");

        final Object[] parameters = context.getParameters();
        parameterChecker.check(parameters);
        final TextMessage message = (TextMessage) parameters[0];

        final JsonObjectBuilder builder = createObjectBuilder();

        addServiceContextNameIfPresent(builder);

        addMetaDataToBuilder(message, builder);

        MDC.put(REQUEST_DATA, builder.build().toString());

        traceLogger.trace(logger, () -> "Request data added to MDC");

        final Object result = context.proceed();

        traceLogger.trace(logger, () -> "Clearing MDC");

        MDC.clear();

        return result;
    }

    private void addServiceContextNameIfPresent(final JsonObjectBuilder builder) {
        Optional.ofNullable(serviceContextNameProvider.getServiceContextName())
                .ifPresent(value -> builder.add(SERVICE_CONTEXT, value));
    }

    private void addMetaDataToBuilder(final TextMessage message, final JsonObjectBuilder builder) {
        try {
            builder.add(METADATA, jmsMessageLoggerHelper.metadataAsJsonObject(message));
        } catch (Exception e) {
            builder.add(METADATA, "Could not find: _metadata in message");
        }
    }
}
