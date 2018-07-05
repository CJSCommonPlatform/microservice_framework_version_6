package uk.gov.justice.services.eventsourcing.source.core;

import static java.lang.String.format;
import static uk.gov.justice.services.eventsourcing.source.core.EventSourceConstants.INITIAL_EVENT_VERSION;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

public class PublishingEventAppender implements EventAppender {

    private final EventRepository eventRepository;

    public PublishingEventAppender(final EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Stores the event in the event store.
     *
     * @param event    - the event to be appended
     * @param streamId - id of the stream the event will be part of
     * @param version  - version id of the event in the stream
     */
    @Override
    public void append(final JsonEnvelope event, final UUID streamId, final long version, final String eventSourceName) throws EventStreamException {
        try {
            if (version == INITIAL_EVENT_VERSION) {
                eventRepository.createEventStream(streamId);
            }
            final JsonEnvelope eventWithStreamIdAndVersion = eventFrom(event, streamId, version, eventSourceName);
            eventRepository.storeEvent(eventWithStreamIdAndVersion);
        } catch (StoreEventRequestFailedException e) {
            throw new EventStreamException(format("Failed to append event to the event store %s", event.metadata().id()), e);
        }
    }
}
