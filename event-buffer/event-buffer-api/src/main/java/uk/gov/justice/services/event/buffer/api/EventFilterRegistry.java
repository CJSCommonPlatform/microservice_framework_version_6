package uk.gov.justice.services.event.buffer.api;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EventFilterRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventFilterRegistry.class);

    @Inject
    Instance<EventFilter> eventFilters;

    @PostConstruct
    void init() {
            LOGGER.warn("****************************** Ading all my event filters");
        for (EventFilter eventFilter : eventFilters) {
            LOGGER.warn("****************************** Ading event filter with events: " + eventFilter.getSupportedEvents().size());
            eventRegistry.addAll(eventFilter.getSupportedEvents());
        }
    }

    private static Set<String> eventRegistry = new HashSet<>();

    public boolean accepts(final String eventName) {
        for (String event : eventRegistry) {
            LOGGER.info("Events contains: " + event);
        }
        return eventRegistry.contains(eventName);
    }

//    public void register(@Observes final EventFilterFoundEvent event) {
//        LOGGER.info("*********** Adding {} events to the registry", event.getEvents().size());
//        eventRegistry.addAll(event.getEvents());
//    }
}
