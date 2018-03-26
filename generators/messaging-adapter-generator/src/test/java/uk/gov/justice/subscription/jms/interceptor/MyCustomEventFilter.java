package uk.gov.justice.subscription.jms.interceptor;

import uk.gov.justice.services.event.buffer.api.EventFilter;

public class MyCustomEventFilter implements EventFilter {

    private final String eventName;

    public MyCustomEventFilter(final String eventName) {
        this.eventName = eventName;
    }

    @Override
    public boolean accepts(final String eventName) {
        return this.eventName.equals(eventName);
    }
}
