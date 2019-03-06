package uk.gov.justice.subscription.domain.eventsource;

import static java.lang.String.format;
import static java.util.Optional.of;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultEventSourceDefinitionFactory {

    private static final String EVENT_STORE_DATA_SOURCE_PATTERN = "java:/app/%s/DS.eventstore";

    @Resource(lookup = "java:app/AppName")
    String warFileName;

    public EventSourceDefinition createDefaultEventSource() {

        final String name = warFileName + "-event-store";
        final String dataSourceName = format(EVENT_STORE_DATA_SOURCE_PATTERN, warFileName);
        final boolean isDefault = true;

        return new EventSourceDefinition(
                name,
                isDefault,
                new Location(
                        "JMS URI not used",
                        "Rest URI not used",
                        of(dataSourceName)
                )
        );
    }

}
