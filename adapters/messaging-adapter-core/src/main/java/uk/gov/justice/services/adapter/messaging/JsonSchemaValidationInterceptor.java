package uk.gov.justice.services.adapter.messaging;

import static java.lang.String.format;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.core.json.JsonSchemaValidator;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.TextMessage;

/**
 * Interceptor for validating messages against a JSON schema.
 */
public class JsonSchemaValidationInterceptor {

    @Inject
    JsonSchemaValidator validator;

    @AroundInvoke
    protected Object validate(final InvocationContext context) throws Exception {
        final Object[] parameters = context.getParameters();

        if (parameters.length != 1) {
            throw new IllegalArgumentException("JSON validation interceptor can only be used on single argument methods");
        }

        if (!(parameters[0] instanceof TextMessage)) {
            throw new IllegalArgumentException(
                    format("JSON validation interceptor can only be used on a JMS TextMessage, not %s", parameters[0].getClass().getName()));
        }

        final TextMessage message = (TextMessage) parameters[0];
        validator.validate(message.getText(), message.getStringProperty(JMS_HEADER_CPPNAME));

        return context.proceed();
    }
}
