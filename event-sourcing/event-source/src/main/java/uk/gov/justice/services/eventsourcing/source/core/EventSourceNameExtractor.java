package uk.gov.justice.services.eventsourcing.source.core;

import static uk.gov.justice.services.core.cdi.EventSourceName.DEFAULT_EVENT_SOURCE_NAME;

import uk.gov.justice.services.core.cdi.EventSourceName;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class EventSourceNameExtractor {

    @SuppressWarnings("unchecked")
    public String getEventSourceNameFromQualifier(final InjectionPoint injectionPoint) {

        return injectionPoint.getQualifiers().stream()
                .filter(annotation -> annotation.annotationType().isAssignableFrom(EventSourceName.class))
                .map(annotation -> ((EventSourceName) annotation).value())
                .findFirst()
                .orElse(DEFAULT_EVENT_SOURCE_NAME);
    }
}
