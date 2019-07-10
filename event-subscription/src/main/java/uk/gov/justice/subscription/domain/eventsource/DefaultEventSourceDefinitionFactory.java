package uk.gov.justice.subscription.domain.eventsource;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.jdbc.persistence.JndiAppNameProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DefaultEventSourceDefinitionFactory {

    private static final String EVENT_STORE_DATA_SOURCE_PATTERN = "java:/app/%s/DS.eventstore";

    @Inject
    private JndiAppNameProvider jndiAppNameProvider;

    public EventSourceDefinition createDefaultEventSource() {

        final String warFileName = jndiAppNameProvider.getAppName();
        final String name = warFileName + "-event-store";
        final String dataSourceName = format(EVENT_STORE_DATA_SOURCE_PATTERN, warFileName);
        final boolean isDefault = true;

        return new EventSourceDefinition(
                name,
                isDefault,
                new Location(
                        "JMS URI not used",
                        empty(),
                        of(dataSourceName)
                )
        );
    }
}
