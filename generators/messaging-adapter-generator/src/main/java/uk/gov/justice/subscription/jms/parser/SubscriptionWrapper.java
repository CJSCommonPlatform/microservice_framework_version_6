package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import java.util.List;
import java.util.Map;

public class SubscriptionWrapper {

    private final Map<String, EventSourceDefinition> eventSourceMap;
    private final SubscriptionDescriptor subscriptionDescriptor;

    public SubscriptionWrapper(final SubscriptionDescriptor subscriptionDescriptor, final List<EventSourceDefinition> eventSourceDefinitions) {
        this.subscriptionDescriptor = subscriptionDescriptor;
        eventSourceMap = eventSourceDefinitions.stream().collect(toMap(EventSourceDefinition::getName, eventSource -> eventSource));
    }

    public SubscriptionDescriptor getSubscriptionDescriptor() {
        return subscriptionDescriptor;
    }

    public EventSourceDefinition getEventSourceByName(final String name) {
        return eventSourceMap.get(name);
    }
}
