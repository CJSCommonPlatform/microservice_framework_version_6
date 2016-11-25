package uk.gov.justice.services.adapter.messaging;

import static java.lang.String.format;
import static uk.gov.justice.services.core.json.JsonValidationLogger.toValidationTrace;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;
import static uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper.toJmsTraceString;

import uk.gov.justice.services.core.eventfilter.EventFilter;
import uk.gov.justice.services.core.json.JsonSchemaValidator;

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
    JmsParametersChecker parametersChecker;

    @Inject
    JsonSchemaValidator validator;

    @Inject
    EventFilter eventFilter;

    @AroundInvoke
    protected Object validate(final InvocationContext context) throws Exception {
        final Object[] parameters = context.getParameters();

        parametersChecker.check(parameters);

        final TextMessage message = (TextMessage) parameters[0];
        final String messageName = message.getStringProperty(JMS_HEADER_CPPNAME);
        if (eventFilter.accepts(messageName)) {
            validate(message);
        }

        return context.proceed();
    }

    private void validate(final TextMessage message) throws JMSException {
        try {
            validator.validate(message.getText(), message.getStringProperty(JMS_HEADER_CPPNAME));
        } catch (ValidationException validationException) {
            logger.debug(format("JSON schema validation has failed for %s due to %s",
                    toJmsTraceString(message),
                    toValidationTrace(validationException)));
            throw validationException;
        }
    }
}
