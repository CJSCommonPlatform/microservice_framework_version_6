package uk.gov.justice.services.adapter.messaging;

import static java.lang.String.format;
import static java.util.Optional.of;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.core.json.JsonSchemaValidationException;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonValidationLoggerHelper;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.slf4j.Logger;

/**
 * Interceptor for validating messages against a JSON schema.
 */
public class JsonSchemaValidationInterceptor {

    @Inject
    Logger logger;

    @Inject
    JmsParameterChecker parametersChecker;

    @Inject
    JsonValidationLoggerHelper jsonValidationLoggerHelper;

    @Inject
    JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Inject
    JsonSchemaValidator jsonSchemaValidator;

    @Inject
    NameToMediaTypeConverter nameToMediaTypeConverter;

    @AroundInvoke
    protected Object validate(final InvocationContext context) throws Exception {
        final Object[] parameters = context.getParameters();

        parametersChecker.check(parameters);

        final TextMessage message = (TextMessage) parameters[0];
        if (shouldValidate(message)) {
            validate(message);
        }

        return context.proceed();
    }

    public boolean shouldValidate(final TextMessage message) throws JMSException {
        return true;
    }

    private void validate(final TextMessage message) throws JMSException {
        try {
            final String name = message.getStringProperty(JMS_HEADER_CPPNAME);
            final MediaType mediaType = nameToMediaTypeConverter.convert(name);

            jsonSchemaValidator.validate(message.getText(), name, of(mediaType));
        } catch (final JsonSchemaValidationException jsonSchemaValidationException) {
            logger.debug(format("JSON schema validation has failed for %s due to %s",
                    jmsMessageLoggerHelper.toJmsTraceString(message),
                    jsonValidationLoggerHelper.toValidationTrace(jsonSchemaValidationException)));
            throw jsonSchemaValidationException;
        }
    }
}
