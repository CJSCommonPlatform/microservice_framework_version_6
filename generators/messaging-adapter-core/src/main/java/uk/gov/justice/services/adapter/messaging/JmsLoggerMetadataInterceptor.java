package uk.gov.justice.services.adapter.messaging;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.common.log.LoggerConstants.METADATA;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;
import static uk.gov.justice.services.common.log.LoggerConstants.SERVICE_CONTEXT;
import static uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper.metadataAsJsonObject;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;

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
    JmsParametersChecker parametersChecker;

    @Inject
    ServiceContextNameProvider serviceContextNameProvider;

    @AroundInvoke
    protected Object addRequestDataToMappedDiagnosticContext(final InvocationContext context) throws Exception {
        trace(logger, () -> "Adding Request data to MDC");

        final Object[] parameters = context.getParameters();
        parametersChecker.check(parameters);
        final TextMessage message = (TextMessage) parameters[0];

        final JsonObjectBuilder builder = createObjectBuilder();

        addServiceContextNameIfPresent(builder);

        addMetaDataToBuilder(message, builder);

        MDC.put(REQUEST_DATA, builder.build().toString());

        trace(logger, () -> "Request data added to MDC");

        final Object result = context.proceed();

        trace(logger, () -> "Clearing MDC");

        MDC.clear();

        return result;
    }

    private void addServiceContextNameIfPresent(final JsonObjectBuilder builder) {
        Optional.ofNullable(serviceContextNameProvider.getServiceContextName())
                .ifPresent(value -> builder.add(SERVICE_CONTEXT, value));
    }

    private void addMetaDataToBuilder(final TextMessage message, final JsonObjectBuilder builder) {
        try {
            builder.add(METADATA, metadataAsJsonObject(message));
        } catch (Exception e) {
            builder.add(METADATA, "Could not find: _metadata in message");
        }
    }
}
