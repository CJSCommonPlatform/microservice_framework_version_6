package uk.gov.justice.services.event.sourcing.subscription;

import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;

import javax.enterprise.util.AnnotationLiteral;

public class EventSourceNameQualifier extends AnnotationLiteral<EventSourceName> implements EventSourceName {

    private final String value;

    public EventSourceNameQualifier(final String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
