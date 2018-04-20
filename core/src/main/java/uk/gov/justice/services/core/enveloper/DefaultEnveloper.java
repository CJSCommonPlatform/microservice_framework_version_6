package uk.gov.justice.services.core.enveloper;

import static java.lang.String.format;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.services.messaging.JsonMetadata.CAUSATION;
import static uk.gov.justice.services.messaging.JsonMetadata.CREATED_AT;
import static uk.gov.justice.services.messaging.JsonMetadata.ID;
import static uk.gov.justice.services.messaging.JsonMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonMetadata.STREAM;
import static uk.gov.justice.services.messaging.JsonMetadata.STREAM_ID;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.enveloper.exception.InvalidEventException;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * Enveloper of POJO classes to the equivalent event envelopes using the event map registry built
 * from {@link Event} annotated classes.
 */
@ApplicationScoped
public class DefaultEnveloper implements Enveloper {

    Clock clock;

    ObjectToJsonValueConverter objectToJsonValueConverter;

    DefaultEnveloper() {
    }

    @Inject
    public DefaultEnveloper(final Clock clock, final ObjectToJsonValueConverter objectToJsonValueConverter) {
        this.clock = clock;
        this.objectToJsonValueConverter = objectToJsonValueConverter;
    }

    private ConcurrentHashMap<Class<?>, String> eventMap = new ConcurrentHashMap<>();

    /**
     * Register method, invoked automatically to register all event classes into the eventMap.
     *
     * @param event identified by the framework to be registered into the event map.
     */
    public void register(@Observes final EventFoundEvent event) {
        eventMap.putIfAbsent(event.getClazz(), event.getEventName());
    }

    public Function<Object, JsonEnvelope> withMetadataFrom(final JsonEnvelope envelope) {
        return x -> envelopeFrom(buildMetaData(x, envelope.metadata()), objectToJsonValueConverter.convert(x));
    }

    public Function<Object, JsonEnvelope> withMetadataFrom(final JsonEnvelope envelope, final String name) {
        return x -> envelopeFrom(buildMetaData(envelope.metadata(), name), x == null ? JsonValue.NULL : objectToJsonValueConverter.convert(x));
    }

    private Metadata buildMetaData(final Object eventObject, final Metadata metadata) {
        if (eventObject == null) {
            throw new IllegalArgumentException("Event object should not be null");
        }

        if (!eventMap.containsKey(eventObject.getClass())) {
            throw new InvalidEventException(format("Failed to map event. No event registered for %s", eventObject.getClass()));
        }

        return buildMetaData(metadata, eventMap.get(eventObject.getClass()));
    }

    private Metadata buildMetaData(final Metadata metadata, final String name) {

        JsonObjectBuilder metadataBuilder = JsonObjects.createObjectBuilderWithFilter(metadata.asJsonObject(),
                x -> !Arrays.asList(ID, NAME, CAUSATION, STREAM).contains(x));

        metadata.streamId().ifPresent(uuid -> metadataBuilder.add(STREAM, Json.createObjectBuilder().add(STREAM_ID, uuid.toString()).build()));

        final JsonObject jsonObject = metadataBuilder
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, name)
                .add(CAUSATION, createCausation(metadata))
                .add(CREATED_AT, ZonedDateTimes.toString(clock.now()))
                .build();

        return metadataFrom(jsonObject).build();
    }

    private JsonArray createCausation(final Metadata metadata) {
        JsonArrayBuilder causation = Json.createArrayBuilder();
        if (metadata.asJsonObject().containsKey(CAUSATION)) {
            metadata.asJsonObject().getJsonArray(CAUSATION).forEach(causation::add);
        }
        causation.add(metadata.id().toString());

        return causation.build();
    }
}
