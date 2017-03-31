package uk.gov.justice.services.messaging.jms;

import javax.jms.Message;
import javax.jms.Session;

/**
 * Interface that specifies a converter between Java objects and JMS messages.
 */
public interface MessageConverter<T extends Object, M extends Message> {

    /**
     * Convert from a JMS Message to a Java object.
     *
     * @param message to be converted.
     * @return converted object.
     */
    T fromMessage(final M message);

    /**
     * Convert a Java object to a JMS Message using the supplied session to create the message
     * object.
     *
     * @param object  to be converted.
     * @param session used to create the Message.
     * @return converted message.
     */
    M toMessage(final T object, final Session session);

}