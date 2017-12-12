package uk.gov.justice.services.adapter.messaging;

import static java.lang.String.format;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonValidationLoggerHelper;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.everit.json.schema.ValidationException;
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
    JsonSchemaValidator validator;

    @Inject
    JsonValidationLoggerHelper jsonValidationLoggerHelper;

    @Inject
    JmsMessageLoggerHelper jmsMessageLoggerHelper;

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

    protected boolean shouldValidate(final TextMessage message) throws JMSException {
        return true;
    }

    private void validate(final TextMessage message) throws JMSException {
        try {
            validator.validate(message.getText(), message.getStringProperty(JMS_HEADER_CPPNAME));
        } catch (ValidationException validationException) {
            logger.debug(format("JSON schema validation has failed for %s due to %s",
                    jmsMessageLoggerHelper.toJmsTraceString(message),
                    jsonValidationLoggerHelper.toValidationTrace(validationException)));
            throw validationException;
        }
    }
}
