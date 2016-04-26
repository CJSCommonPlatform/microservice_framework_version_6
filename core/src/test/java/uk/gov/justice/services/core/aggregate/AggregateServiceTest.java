package uk.gov.justice.services.core.aggregate;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Event;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link AggregateService} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class AggregateServiceTest {

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Mock
    private EventStream eventStream;

    private AggregateService aggregateService;

    @Before
    public void setup() {
        aggregateService = new AggregateService();
        aggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Test
    public void shouldCreateAggregateFromEmptyStream() {
        when(eventStream.read()).thenReturn(Stream.empty());
        RecordingAggregate aggregate = aggregateService.get(eventStream, RecordingAggregate.class);
        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents, empty());
    }

    @Test
    public void shouldCreateAggregateFromSingletonStream() {
        JsonObject eventPayloadA = mock(JsonObject.class);
        EventA eventA = mock(EventA.class);
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.read()).thenReturn(Stream.of(createEnvelope("eventA", eventPayloadA)));

        aggregateService.register(new EventFoundEvent(EventA.class, "eventA"));

        RecordingAggregate aggregate = aggregateService.get(eventStream, RecordingAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents, hasSize(1));
        assertThat(aggregate.recordedEvents.get(0), equalTo(eventA));
    }

    @Test
    public void shouldCreateAggregateFromStreamOfTwo() {
        JsonObject eventPayloadA = mock(JsonObject.class);
        JsonObject eventPayloadB = mock(JsonObject.class);
        EventA eventA = mock(EventA.class);
        EventB eventB = mock(EventB.class);
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(jsonObjectToObjectConverter.convert(eventPayloadB, EventB.class)).thenReturn(eventB);
        when(eventStream.read()).thenReturn(Stream.of(
                createEnvelope("eventA", eventPayloadA),
                createEnvelope("eventB", eventPayloadB)));

        aggregateService.register(new EventFoundEvent(EventA.class, "eventA"));
        aggregateService.register(new EventFoundEvent(EventB.class, "eventB"));

        RecordingAggregate aggregate = aggregateService.get(eventStream, RecordingAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents, hasSize(2));
        assertThat(aggregate.recordedEvents.get(0), equalTo(eventA));
        assertThat(aggregate.recordedEvents.get(1), equalTo(eventB));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForUnregisteredEvent() {
        JsonObject eventPayloadA = mock(JsonObject.class);
        EventA eventA = mock(EventA.class);
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.read()).thenReturn(Stream.of(createEnvelope("eventA", eventPayloadA)));

        aggregateService.get(eventStream, RecordingAggregate.class);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForNonInstantiatableEvent() {
        JsonObject eventPayloadA = mock(JsonObject.class);
        EventA eventA = mock(EventA.class);
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.read()).thenReturn(Stream.of(createEnvelope("eventA", eventPayloadA)));

        aggregateService.register(new EventFoundEvent(EventA.class, "eventA"));

        aggregateService.get(eventStream, PrivateAggregate.class);
    }

    private static JsonEnvelope createEnvelope(final String name, final JsonObject payload) {
        return envelopeFrom(
                metadataFrom(
                        createObjectBuilder()
                                .add(ID, UUID.randomUUID().toString())
                                .add(NAME, name)
                                .build()),
                payload);
    }

    public static class RecordingAggregate implements Aggregate {

        List<Object> recordedEvents = new ArrayList<>();

        @Override
        public Object apply(Object event) {
            recordedEvents.add(event);
            return event;
        }
    }

    private static class PrivateAggregate implements Aggregate {

        @Override
        public Object apply(Object event) {
            return event;
        }
    }

    @Event("eventA")
    public static class EventA {

    }

    @Event("eventB")
    public static class EventB {

    }
}
