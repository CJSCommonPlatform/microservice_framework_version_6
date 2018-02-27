package uk.gov.justice.services.core.enveloper;

import static java.lang.String.format;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.services.messaging.JsonMetadata.CAUSATION;
import static uk.gov.justice.services.messaging.JsonMetadata.CREATED_AT;
import static uk.gov.justice.services.messaging.JsonMetadata.ID;
import static uk.gov.justice.services.messaging.JsonMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonMetadata.STREAM;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.enveloper.exception.InvalidEventException;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.messaging.Envelope;
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

    private static ConcurrentHashMap<Class<?>, String> eventMap = new ConcurrentHashMap<>();

    private Clock clock;

    private ObjectToJsonValueConverter objectToJsonValueConverter;

    public DefaultEnveloper() {
    }

    @Inject
    public DefaultEnveloper(final Clock clock, final ObjectToJsonValueConverter objectToJsonValueConverter) {
        this.clock = clock;
        this.objectToJsonValueConverter = objectToJsonValueConverter;
    }

    /**
     * Register method, invoked automatically to register all event classes into the eventMap.
     *
     * @param event identified by the framework to be registered into the event map.
     */
    public void register(@Observes final EventFoundEvent event) {
        eventMap.putIfAbsent(event.getClazz(), event.getEventName());
    }

    @Override
    public Function<Object, JsonEnvelope> withMetadataFrom(final JsonEnvelope envelope) {
        return x -> envelopeFrom(buildMetaData(x, envelope.metadata(), clock), objectToJsonValueConverter.convert(x));
    }

    @Override
    public Function<Object, JsonEnvelope> withMetadataFrom(final JsonEnvelope envelope, final String name) {
        return x -> envelopeFrom(buildMetaData(envelope.metadata(), name, clock), x == null ? JsonValue.NULL : objectToJsonValueConverter.convert(x));
    }

    public Function<Object, JsonEnvelope> toEnvelopeWithMetadataFrom(final Envelope<?> envelope) {
        return x -> envelopeFrom(buildMetaData(x, envelope.metadata(), clock), objectToJsonValueConverter.convert(x));
    }

    @SuppressWarnings("unchecked")
    public <T> EnveloperBuilder<T> envelop(final T payload) {
        return new DefaultEnveloperBuilder<>(payload);
    }

    private Metadata buildMetaData(final Object eventObject, final Metadata metadata, final Clock clock) {
        if (eventObject == null) {
            throw new IllegalArgumentException("Event object should not be null");
        }

        if (!eventMap.containsKey(eventObject.getClass())) {
            throw new InvalidEventException(format("Failed to map event. No event registered for %s", eventObject.getClass()));
        }

        return buildMetaData(metadata, eventMap.get(eventObject.getClass()), clock);
    }

    private Metadata buildMetaData(final Metadata metadata, final String name, final Clock clock) {

        final JsonObjectBuilder metadataBuilder = JsonObjects.createObjectBuilderWithFilter(metadata.asJsonObject(),
                x -> !Arrays.asList(ID, NAME, CAUSATION, STREAM).contains(x));

        final JsonObject jsonObject = metadataBuilder
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, name)
                .add(CAUSATION, createCausation(metadata))
                .add(CREATED_AT, ZonedDateTimes.toString(clock.now()))
                .build();

        return metadataFrom(jsonObject).build();
    }

    private JsonArray createCausation(final Metadata metadata) {
        final JsonArrayBuilder causation = Json.createArrayBuilder();

        if (metadata.asJsonObject().containsKey(CAUSATION)) {
            metadata.asJsonObject().getJsonArray(CAUSATION).forEach(causation::add);
        }

        return causation
                .add(metadata.id().toString())
                .build();
    }

    private class DefaultEnveloperBuilder<T> implements EnveloperBuilder {

        private String name;
        private T payload;

        private DefaultEnveloperBuilder(final T payload) {
            this.payload = payload;
        }

        private void setName(final String name) {
            this.name = name;
        }

        @Override
        public EnveloperBuilder withName(final String name) {
            this.setName(name);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Envelope withMetadataFrom(final Envelope envelope) {
            return envelopeFrom(buildMetaData(envelope.metadata(), this.name, clock), this.payload);
        }

    }

}
