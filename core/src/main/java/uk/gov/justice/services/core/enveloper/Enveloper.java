package uk.gov.justice.services.core.enveloper;

import static java.lang.String.format;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CAUSATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
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
public class Enveloper {

    private final ObjectToJsonValueConverter objectToJsonValueConverter;

    //for CDI proxying only
    protected Enveloper() {
        this.objectToJsonValueConverter = null;
    }

    @Inject
    public Enveloper(final ObjectToJsonValueConverter objectToJsonValueConverter) {
        this.objectToJsonValueConverter = objectToJsonValueConverter;
    }

    private ConcurrentHashMap<Class<?>, String> eventMap = new ConcurrentHashMap<>();

    /**
     * Register method, invoked automatically to register all event classes into the eventMap.
     *
     * @param event identified by the framework to be registered into the event map.
     */
    void register(@Observes final EventFoundEvent event) {
        eventMap.putIfAbsent(event.getClazz(), event.getEventName());
    }

    /**
     * Provides a function that wraps the provided object into a new {@link JsonEnvelope} using
     * metadata from the given envelope.
     *
     * @param envelope - the envelope containing source metadata.
     * @return a function that wraps objects into an envelope.
     */
    public Function<Object, JsonEnvelope> withMetadataFrom(final JsonEnvelope envelope) {
        return x -> envelopeFrom(buildMetaData(x, envelope.metadata()), objectToJsonValueConverter.convert(x));
    }

    /**
     * Provides a function that wraps the provided object into a new {@link JsonEnvelope} using
     * metadata from the given envelope, except the name.
     *
     * @param envelope - the envelope containing source metadata.
     * @param name     - name of the payload.
     * @return a function that wraps objects into an envelope.
     */
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
                x -> !Arrays.asList(ID, NAME, CAUSATION).contains(x));

        final JsonObject jsonObject = metadataBuilder
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, name)
                .add(CAUSATION, createCausation(metadata))
                .build();

        return metadataFrom(jsonObject);
    }

    private JsonArray createCausation(final Metadata metadata) {
        JsonArrayBuilder causation = Json.createArrayBuilder();
        if (metadata.asJsonObject().containsKey(CAUSATION)) {
            metadata.asJsonObject().getJsonArray(CAUSATION).stream().forEach(causation::add);
        }
        causation.add(metadata.id().toString());

        return causation.build();
    }

}
