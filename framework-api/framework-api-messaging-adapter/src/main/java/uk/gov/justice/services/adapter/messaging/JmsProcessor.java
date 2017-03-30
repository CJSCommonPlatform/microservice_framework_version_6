package uk.gov.justice.services.adapter.messaging;

import uk.gov.justice.services.core.interceptor.InterceptorContext;

import java.util.function.Consumer;

import javax.jms.Message;

public interface JmsProcessor {

    /**
     * Process an incoming JMS message by validating the message and then passing the envelope
     * converted from the message to the given consumer.
     *
     * @param consumer a consumer for the envelope
     * @param message  a message to be processed
     */
    void process(final Consumer<InterceptorContext> consumer, final Message message);
}