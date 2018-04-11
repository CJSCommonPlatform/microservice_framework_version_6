package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import java.util.List;
import java.util.Map;

public class SubscriptionWrapper {

    private final Map<String, EventSource> eventSourceMap;
    private final SubscriptionDescriptor subscriptionDescriptor;

    public SubscriptionWrapper(final SubscriptionDescriptor subscriptionDescriptor, final List<EventSource> eventSources) {
        this.subscriptionDescriptor = subscriptionDescriptor;
        eventSourceMap = eventSources.stream().collect(toMap(EventSource::getName, eventSource -> eventSource));
    }

    public SubscriptionDescriptor getSubscriptionDescriptor() {
        return subscriptionDescriptor;
    }

    public EventSource getEventSourceByName(final String name) {
        return eventSourceMap.get(name);
    }
}
