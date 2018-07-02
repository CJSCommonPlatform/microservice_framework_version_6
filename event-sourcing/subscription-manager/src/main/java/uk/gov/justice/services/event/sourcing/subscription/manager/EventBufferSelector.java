package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.event.buffer.api.EventBufferService;

import java.util.Optional;

import javax.inject.Inject;

public class EventBufferSelector {

    @Inject
    EventBufferService eventBufferService;

    public Optional<EventBufferService> selectFor(final String componentName) {

        if (componentName.contains(EVENT_LISTENER)) {
            return of(eventBufferService);
        }

        return empty();
    }
}
