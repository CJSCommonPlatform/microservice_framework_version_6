package uk.gov.justice.services.eventsourcing.source.core;


import static java.lang.String.format;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventAppender {

    @Inject
    EventRepository eventRepository;

    @Inject
    EventPublisher eventPublisher;

    /**
     * Stores the event in the event store and publishes it with the given streamId and version.
     *
     * @param event - the event to be appended
     * @param streamId - id of the stream the event will be part of
     * @param version - version id of the event in the stream
     * @throws EventStreamException
     */
    void append(final JsonEnvelope event, final UUID streamId, final long version) throws EventStreamException {
        try {
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
