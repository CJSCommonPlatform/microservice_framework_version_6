package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

/**
 * Converter class to convert between {@link JsonEnvelope} and {@link Event}
 */
@ApplicationScoped
public class EventConverter {

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    StringToJsonObjectConverter stringToJsonObjectConverter;


    /**
     * Creates an {@link Event} object from the <code>eventEnvelope</code>.
     *
     * @param envelope the envelope to convert from
     * @return the database entity created from the given envelope
     */
    public Event eventOf(final JsonEnvelope envelope) {

        final Metadata eventMetadata = envelope.metadata();

        return new Event(eventMetadata.id(),
                eventMetadata.streamId().orElseThrow(() -> new InvalidStreamIdException("StreamId missing in envelope.")),
                eventMetadata.version().orElse(null),
                eventMetadata.name(),
                envelope.metadata().asJsonObject().toString(),
                extractPayloadAsString(envelope),
                eventMetadata
                        .createdAt()
                        .map(createdAt -> createdAt)
                        .orElseThrow(() -> new IllegalArgumentException("createdAt field missing in envelope")
                        ));
    }

    /**
     * Creates an {@link JsonEnvelope} from {@link Event}
     *
     * @param event event to be converted into an envelope.
     * @return an envelope created from event.
     */
    public JsonEnvelope envelopeOf(final Event event) {
        return envelopeFrom(metadataOf(event), payloadOf(event));
    }

    /**
     * Retrieves metadata from event.
     *
     * @param event event containing the metadata.
     * @return metadata from the event.
     */
    public Metadata metadataOf(final Event event) {
        return metadataFrom(stringToJsonObjectConverter.convert(event.getMetadata())).build();
    }

    private JsonObject payloadOf(final Event event) {
        return stringToJsonObjectConverter.convert(event.getPayload());
    }

    private String extractPayloadAsString(final JsonEnvelope envelope) {
        return jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(
                jsonObjectEnvelopeConverter.fromEnvelope(envelope)).toString();
    }

}
