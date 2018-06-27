package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.List;
import java.util.Map;

public class SubscriptionWrapper {

    private final Map<String, EventSourceDefinition> eventSourceMap;
    private final SubscriptionsDescriptor subscriptionsDescriptor;

    public SubscriptionWrapper(final SubscriptionsDescriptor subscriptionsDescriptor, final List<EventSourceDefinition> eventSourceDefinitions) {
        this.subscriptionsDescriptor = subscriptionsDescriptor;
        eventSourceMap = eventSourceDefinitions.stream().collect(toMap(EventSourceDefinition::getName, eventSource -> eventSource));
    }

    public SubscriptionsDescriptor getSubscriptionsDescriptor() {
        return subscriptionsDescriptor;
    }

    public EventSourceDefinition getEventSourceByName(final String name) {
        return eventSourceMap.get(name);
    }
}
