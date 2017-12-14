package uk.gov.justice.services.core.aggregate;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.domain.aggregate.PrivateAggregate;
import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.event.EventA;
import uk.gov.justice.services.core.aggregate.event.EventB;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;

import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Unit tests for the {@link DefaultAggregateService} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAggregateServiceTest {

    private static final UUID STREAM_ID = randomUUID();

    @Mock
    private Logger logger;

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Mock
    private EventStream eventStream;

    @InjectMocks
    private DefaultAggregateService aggregateService;

    private void registerEvent(Class clazz, String name) {
        aggregateService.register(new EventFoundEvent(clazz, name));
    }

    @Test
    public void shouldCreateAggregateFromEmptyStream() {
        when(eventStream.read()).thenReturn(Stream.empty());
        when(eventStream.getId()).thenReturn(STREAM_ID);
        TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), empty());
        verify(logger).trace("Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
    }

    @Test
    public void shouldCreateAggregateFromSingletonStream() {
        JsonObject eventPayloadA = mock(JsonObject.class);
        EventA eventA = mock(EventA.class);
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.read()).thenReturn(Stream.of(envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA)));
        when(eventStream.getId()).thenReturn(STREAM_ID);

        registerEvent(EventA.class, "eventA");

        TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), hasSize(1));
        assertThat(aggregate.recordedEvents().get(0), equalTo(eventA));
        verify(logger).info("Registering event {}, {} with DefaultAggregateService", "eventA", EventA.class);
        verify(logger).trace("Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
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
                envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA),
                envelopeFrom(metadataWithRandomUUID("eventB"), eventPayloadB)));
        when(eventStream.getId()).thenReturn(STREAM_ID);

        registerEvent(EventA.class, "eventA");
        registerEvent(EventB.class, "eventB");

        TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), hasSize(2));
        assertThat(aggregate.recordedEvents().get(0), equalTo(eventA));
        assertThat(aggregate.recordedEvents().get(1), equalTo(eventB));
        verify(logger).info("Registering event {}, {} with DefaultAggregateService", "eventA", EventA.class);
        verify(logger).info("Registering event {}, {} with DefaultAggregateService", "eventB", EventB.class);
        verify(logger).trace("Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
    }

    @Test
    public void shouldCreateAggregateFromStreamOfThreeWithAFilteredOutSystemEvent() {
        JsonObject eventPayloadA = mock(JsonObject.class);
        JsonObject eventPayloadB = mock(JsonObject.class);

        EventA eventA = mock(EventA.class);
        EventB eventB = mock(EventB.class);
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(jsonObjectToObjectConverter.convert(eventPayloadB, EventB.class)).thenReturn(eventB);
        when(eventStream.read()).thenReturn(Stream.of(
                envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA),
                envelopeFrom(metadataWithRandomUUID("eventB"), eventPayloadB),
                envelopeFrom(metadataWithRandomUUID("system.events.eventC"), eventPayloadB)));

        when(eventStream.getId()).thenReturn(STREAM_ID);

        aggregateService.register(new EventFoundEvent(EventA.class, "eventA"));
        aggregateService.register(new EventFoundEvent(EventB.class, "eventB"));

        TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), hasSize(2));
        assertThat(aggregate.recordedEvents().get(0), equalTo(eventA));
        assertThat(aggregate.recordedEvents().get(1), equalTo(eventB));
        verify(logger).info("Registering event {}, {} with DefaultAggregateService", "eventA" , EventA.class);
        verify(logger).info("Registering event {}, {} with DefaultAggregateService", "eventB" , EventB.class);
        verify(logger).trace("Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForUnregisteredEvent() {
        when(eventStream.getId()).thenReturn(STREAM_ID);

        JsonObject eventPayloadA = mock(JsonObject.class);
        EventA eventA = mock(EventA.class);
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.read()).thenReturn(Stream.of(envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA)));

        aggregateService.get(eventStream, TestAggregate.class);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForNonInstantiatableEvent() {
        JsonObject eventPayloadA = mock(JsonObject.class);
        EventA eventA = mock(EventA.class);
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.read()).thenReturn(Stream.of(envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA)));

        registerEvent(EventA.class, "eventA");

        aggregateService.get(eventStream, PrivateAggregate.class);
    }


}
