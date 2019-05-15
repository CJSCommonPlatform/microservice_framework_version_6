package uk.gov.justice.services.messaging.jms;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.jms.exception.JmsEnvelopeSenderException;

import javax.inject.Inject;
import javax.jms.Destination;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class DestinationProvider {

    @Inject
    private InitialContext initialContext;

    public Destination getDestination(final String destinationName) {

        try {
            return (Destination) initialContext.lookup(destinationName);
        } catch (final NamingException e) {
            throw new JmsEnvelopeSenderException(format("Exception while looking up JMS destination name '%s'", destinationName), e);
        }
    }
}
