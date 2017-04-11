package uk.gov.justice.services.event.buffer.api;


import static java.util.Collections.addAll;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic event filter implementation, allowing event names that have been configured in the constructor
 */
public abstract class AbstractEventFilter implements EventFilter {
    private final Set<String> allowedEventNames = new HashSet<>();

    /**
     * @param eventNames - names of events that should be considered allowed by the filter
     */
    public AbstractEventFilter(final String... eventNames) {
        addAll(allowedEventNames, eventNames);
    }

    @Override
    public boolean accepts(final String eventName) {
        return allowedEventNames.contains(eventName);
    }
}
