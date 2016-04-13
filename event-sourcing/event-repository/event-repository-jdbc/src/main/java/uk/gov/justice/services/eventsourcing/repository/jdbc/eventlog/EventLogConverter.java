package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;

import uk.gov.justice.services.common.converter.JsonObjectToStringConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.UUID;

/**
 * Converter class to convert between {@link JsonEnvelope} and {@link EventLog}
 */
@ApplicationScoped
public class EventLogConverter {

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    JsonObjectToStringConverter jsonObjectToStringConverter;

    @Inject
    StringToJsonObjectConverter stringToJsonObjectConverter;

    /**
     * Creates an {@link EventLog} object from the <code>eventEnvelope</code>.
     *
     * @param jsonEnvelope the jsonEnvelope to convert from
     * @param streamId the stream id the event belongs to
     * @param version  the version of the event
     * @return the database entity created from the given jsonEnvelope
     */
    public EventLog createEventLog(final JsonEnvelope jsonEnvelope, final UUID streamId, final Long version) {

        if (streamId == null) {
            throw new InvalidStreamIdException("StreamId missing in jsonEnvelope.");
        }

        final Metadata eventMetadata = jsonEnvelope.metadata();

        return new EventLog(eventMetadata.id(),
                streamId,
                version,
                eventMetadata.name(),
                jsonObjectToStringConverter.convert(jsonEnvelope.metadata().asJsonObject()),
                extractPayloadAsString(jsonEnvelope));

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

    private String extractPayloadAsString(final JsonEnvelope jsonEnvelope) {
        return jsonObjectToStringConverter.convert(jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(
                jsonObjectEnvelopeConverter.fromEnvelope(jsonEnvelope)));
    }

}
