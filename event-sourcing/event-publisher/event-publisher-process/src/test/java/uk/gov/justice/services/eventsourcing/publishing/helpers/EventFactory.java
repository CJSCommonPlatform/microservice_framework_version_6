package uk.gov.justice.services.eventsourcing.publishing.helpers;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;
import java.util.UUID;

public class EventFactory {

    public Event createEvent(final String name, final long sequenceId) {
        final UUID eventId = randomUUID();
        final UUID streamId = randomUUID();
        final String source = "event source";
        final JsonEnvelope envelope = envelopeFrom(
                metadataBuilder()
                        .withId(eventId)
                        .withName(name)
                        .withStreamId(streamId)
                        .withSource(source),
                createObjectBuilder()
                        .add("field_" + sequenceId, "value_" + sequenceId));

        final String payload = envelope.payload().toString();
        final String metadata = envelope.metadata().asJsonObject().toString();

        final ZonedDateTime createdAt = new UtcClock().now();

        return new Event(
                eventId,
                streamId,
                sequenceId,
                name,
                metadata,
                payload,
                createdAt);
    }
}
