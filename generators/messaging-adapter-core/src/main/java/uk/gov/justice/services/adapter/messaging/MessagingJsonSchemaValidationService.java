package uk.gov.justice.services.adapter.messaging;

import static java.lang.String.format;
import static uk.gov.justice.services.messaging.SchemaUtil.qualifiedSchemaFilePathFrom;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonValidationLoggerHelper;
import uk.gov.justice.services.event.buffer.api.EventFilter;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.transaction.Transactional;

import org.everit.json.schema.ValidationException;
import org.slf4j.Logger;

/**
 * Interceptor for validating messages against a JSON schema.
 */
@ApplicationScoped
public class MessagingJsonSchemaValidationService {

    @Inject
    Logger logger;

    @Inject
    JsonSchemaValidator validator;

    @Inject
    EventFilter eventFilter;

    @Inject
    JsonValidationLoggerHelper jsonValidationLoggerHelper;

    @Inject
    JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Transactional(dontRollbackOn = JsonSchemaValidationException.class)
    public void validate(final Message message, final String component) {
        try {
            final String messageName = message.getStringProperty(JMS_HEADER_CPPNAME);
            if (eventFilter.accepts(messageName)) {
                validate(component, (TextMessage) message);
            }
        } catch (Exception e) {
            throw new JsonSchemaValidationException("Schema validation failed.", e);
        }
    }

    private void validate(final String component, final TextMessage message) throws JMSException {
        try {
            validator.validate(message.getText(), schemaPathFrom(message, component));
        } catch (ValidationException validationException) {
            try {
                validator.validate(message.getText(), message.getStringProperty(JMS_HEADER_CPPNAME));
            } catch (ValidationException validationExceptionNonQualifiedSchema) {
                logger.debug(format("JSON schema validation has failed for %s due to %s",
                        jmsMessageLoggerHelper.toJmsTraceString(message),
                        jsonValidationLoggerHelper.toValidationTrace(validationExceptionNonQualifiedSchema)));
                throw validationException;
            }
        }
    }

    private String schemaPathFrom(final Message message, final String component) throws JMSException {
        return qualifiedSchemaFilePathFrom(component, message.getStringProperty(JMS_HEADER_CPPNAME));
    }
}