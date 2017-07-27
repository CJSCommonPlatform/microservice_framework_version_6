package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;

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
 * Converter class to convert between {@link JsonEnvelope} and {@link EventLog}
 */
@ApplicationScoped
public class EventLogConverter {

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    StringToJsonObjectConverter stringToJsonObjectConverter;


    /**
     * Creates an {@link EventLog} object from the <code>eventEnvelope</code>.
     *
     * @param envelope the envelope to convert from
     * @return the database entity created from the given envelope
     */
    public EventLog eventLogOf(final JsonEnvelope envelope) {

        final Metadata eventMetadata = envelope.metadata();

        return new EventLog(eventMetadata.id(),
                eventMetadata.streamId().orElseThrow(() -> new InvalidStreamIdException("StreamId missing in envelope.")),
                eventMetadata.version().orElse(null),
                eventMetadata.name(),
                envelope.metadata().asJsonObject().toString(),
                extractPayloadAsString(envelope),
                eventMetadata
                        .createdAt()
                        .map(createdAt -> createdAt)
                        .orElseThrow(() -> new IllegalArgumentException("createdAt field missing in envelope")));
    }

    /**
     * Creates an {@link JsonEnvelope} from {@link EventLog}
     *
     * @param eventLog eventLog to be converted into an envelope.
     * @return an envelope created from eventLog.
     */
    public JsonEnvelope envelopeOf(final EventLog eventLog) {
        return envelopeFrom(metadataOf(eventLog), payloadOf(eventLog));
    }

    /**
     * Retrieves metadata from eventLog.
     *
     * @param eventLog eventLog containing the metadata.
     * @return metadata from the eventLog.
     */
    public Metadata metadataOf(final EventLog eventLog) {
        return metadataFrom(stringToJsonObjectConverter.convert(eventLog.getMetadata())).build();
    }

    private JsonObject payloadOf(final EventLog eventLog) {
        return stringToJsonObjectConverter.convert(eventLog.getPayload());
    }

    private String extractPayloadAsString(final JsonEnvelope envelope) {
        return jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(
                jsonObjectEnvelopeConverter.fromEnvelope(envelope)).toString();
    }

}
