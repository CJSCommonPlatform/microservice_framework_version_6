package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;

import java.util.List;
import java.util.Map;

public class SubscriptionWrapper {

    private final Map<String, EventSourceDefinition> eventSourceMap;
    private final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition;

    public SubscriptionWrapper(final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition, final List<EventSourceDefinition> eventSourceDefinitions) {
        this.subscriptionDescriptorDefinition = subscriptionDescriptorDefinition;
        eventSourceMap = eventSourceDefinitions.stream().collect(toMap(EventSourceDefinition::getName, eventSource -> eventSource));
    }

    public SubscriptionDescriptorDefinition getSubscriptionDescriptorDefinition() {
        return subscriptionDescriptorDefinition;
    }

    public EventSourceDefinition getEventSourceByName(final String name) {
        return eventSourceMap.get(name);
    }
}
