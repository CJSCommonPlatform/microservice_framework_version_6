package uk.gov.justice.services.eventsourcing.source.core;


import static java.lang.String.format;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventAppender {

    private static final long INITIAL_STREAM_EVENT = 1L;
    @Inject
    EventRepository eventRepository;

    @Inject
    EventStreamJdbcRepository streamRepository;

    @Inject
    EventPublisher eventPublisher;

    /**
     * Stores the event in the event store and publishes it with the given streamId and version.
     *
     * @param event    - the event to be appended
     * @param streamId - id of the stream the event will be part of
     * @param version  - version id of the event in the stream
     */
    void append(final JsonEnvelope event, final UUID streamId, final long version) throws EventStreamException {
        try {
            if (version == INITIAL_STREAM_EVENT) {
                streamRepository.insert(new EventStream(streamId));
            }
            final JsonEnvelope eventWithStreamIdAndVersion = eventFrom(event, streamId, version);
            eventRepository.store(eventWithStreamIdAndVersion);
            eventPublisher.publish(eventWithStreamIdAndVersion);
        } catch (StoreEventRequestFailedException e) {
            throw new EventStreamException(format("Failed to append event to the event store %s", event.metadata().id()), e);
        }
    }

    private JsonEnvelope eventFrom(final JsonEnvelope event, final UUID streamId, final long version) {
        return envelopeFrom(metadataFrom(event.metadata()).withStreamId(streamId).withVersion(version), event.payloadAsJsonObject());
    }

}
