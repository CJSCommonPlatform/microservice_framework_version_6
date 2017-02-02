package uk.gov.justice.services.core.jms;

import javax.jms.Destination;

public interface JmsDestinations {

    Destination getDestination(final String component, final String contextName);
}
