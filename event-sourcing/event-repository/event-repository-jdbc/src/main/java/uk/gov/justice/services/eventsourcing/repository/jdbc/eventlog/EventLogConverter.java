package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

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

    @Inject
    Clock clock;

    /**
     * Creates an {@link EventLog} object from the <code>eventEnvelope</code>.
     *
     * @param envelope the envelope to convert from
     * @param streamId the stream id the event belongs to
     * @param version  the version of the event
     * @return the database entity created from the given envelope
     */
    public EventLog createEventLog(final JsonEnvelope envelope, final UUID streamId, final Long version) {

        if (streamId == null) {
            throw new InvalidStreamIdException("StreamId missing in envelope.");
        }

        final Metadata eventMetadata = envelope.metadata();

        return new EventLog(eventMetadata.id(),
                streamId,
                version,
                eventMetadata.name(),
                envelope.metadata().asJsonObject().toString(),
                extractPayloadAsString(envelope),
                clock.now());
    }

    /**
     * Creates an {@link JsonEnvelope} from {@link EventLog}
     *
     * @param eventLog eventLog to be converted into an envelope.
     * @return an envelope created from eventLog.
     */
    public JsonEnvelope createEnvelope(final EventLog eventLog) {
        return DefaultJsonEnvelope.envelopeFrom(getMetaData(eventLog), getPayload(eventLog));
    }

    /**
     * Retrieves metadata from eventLog.
     *
     * @param eventLog eventLog containing the metadata.
     * @return metadata from the eventLog.
     */
    public Metadata getMetaData(final EventLog eventLog) {
        return JsonObjectMetadata.metadataFrom(stringToJsonObjectConverter.convert(eventLog.getMetadata()));
    }

    private JsonObject getPayload(final EventLog eventLog) {
        return stringToJsonObjectConverter.convert(eventLog.getPayload());
    }

    private String extractPayloadAsString(final JsonEnvelope envelope) {
        return jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(
                jsonObjectEnvelopeConverter.fromEnvelope(envelope)).toString();
    }

}
