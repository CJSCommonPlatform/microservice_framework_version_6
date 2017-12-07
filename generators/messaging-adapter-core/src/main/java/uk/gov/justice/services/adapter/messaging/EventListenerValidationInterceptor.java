package uk.gov.justice.services.adapter.messaging;

import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.event.buffer.api.EventFilter;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;

public class EventListenerValidationInterceptor extends JsonSchemaValidationInterceptor {

    @Inject
    EventFilter eventFilter;

    @Override
    public boolean shouldValidate(final TextMessage message) throws JMSException {
        final String messageName = message.getStringProperty(JMS_HEADER_CPPNAME);
        return eventFilter.accepts(messageName);
    }
}
