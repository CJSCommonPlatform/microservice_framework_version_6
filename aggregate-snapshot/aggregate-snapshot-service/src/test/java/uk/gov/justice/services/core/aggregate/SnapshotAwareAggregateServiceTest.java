package uk.gov.justice.services.core.aggregate;

import static java.util.UUID.randomUUID;
import static java.util.stream.Stream.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.domain.aggregate.PrivateAggregate;
import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.domain.event.EventA;
import uk.gov.justice.domain.event.EventB;
import uk.gov.justice.domain.event.EventC;
import uk.gov.justice.domain.snapshot.VersionedAggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.exception.AggregateChangeDetectedException;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Unit tests for the {@link SnapshotAwareAggregateService} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class SnapshotAwareAggregateServiceTest {

    private static final UUID STREAM_ID = randomUUID();
    private static final long INITIAL_AGGREGATE_VERSION = 0L;
    private static final long NEXT_AGGREGATE_VERSION = 1L;

    @Mock
    private Logger logger;

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Mock
    private EventStream eventStream;

    @Mock
    private SnapshotService snapshotService;

    @Spy
    private DefaultAggregateService defaultAggregateService;

    @InjectMocks
    private SnapshotAwareAggregateService aggregateService;

    private void registerEvent(Class clazz, String name) {
        defaultAggregateService.register(new EventFoundEvent(clazz, name));
    }

    @Before
    public void setup() {
        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Test
    public void shouldCreateAggregateFromEmptyStream() throws AggregateChangeDetectedException {
        when(eventStream.getId()).thenReturn(STREAM_ID);
        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(Optional.of(new VersionedAggregate<>(INITIAL_AGGREGATE_VERSION, new TestAggregate())));
        when(eventStream.readFrom(NEXT_AGGREGATE_VERSION)).thenReturn(Stream.empty());

        final TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), empty());
        verify(logger).trace("SnapshotAwareAggregateService Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
    }

    @Test
    public void shouldCreateAggregateFromStreamWithOneEvent() throws AggregateChangeDetectedException {
        Optional<VersionedAggregate<TestAggregate>> versionedAggregate = Optional.empty();

        final EventA eventA = new EventA();
        final JsonObject eventPayloadA = mock(JsonObject.class);

        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.getId()).thenReturn(STREAM_ID);
        when(eventStream.read()).thenReturn(of(
                envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA)));
        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(versionedAggregate);

        registerEvent(EventA.class, "eventA");
        registerEvent(EventB.class, "eventB");

        final TestAggregate aggregateActual = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregateActual, notNullValue());
        assertThat(aggregateActual.recordedEvents(), hasSize(1));
        assertThat(aggregateActual.recordedEvents(), hasItems(eventA));
        verify(logger).info("Registering event {}, {} with DefaultAggregateService", "eventA", EventA.class);
        verify(logger).trace("Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
        verify(logger).trace("SnapshotAwareAggregateService Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
    }

    @Test
    public void shouldCreateAggregateFromStreamWithTwoEvents() throws AggregateChangeDetectedException {
        final EventA eventA = new EventA();
        final EventB eventB = mock(EventB.class);
        final JsonObject eventPayloadA = mock(JsonObject.class);
        final JsonObject eventPayloadB = mock(JsonObject.class);
        final Optional<VersionedAggregate<TestAggregate>> versionedAggregate = Optional.empty();

        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(jsonObjectToObjectConverter.convert(eventPayloadB, EventB.class)).thenReturn(eventB);
        when(eventStream.getId()).thenReturn(STREAM_ID);
        when(eventStream.read()).thenReturn(of(
                envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA), envelopeFrom(metadataWithRandomUUID("eventB"), eventPayloadB)));
        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(versionedAggregate);

        registerEvent(EventA.class, "eventA");
        registerEvent(EventB.class, "eventB");

        final TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), hasSize(2));
        assertThat(aggregate.recordedEvents().get(0), equalTo(eventA));
        assertThat(aggregate.recordedEvents().get(1), equalTo(eventB));
        verify(logger).info("Registering event {}, {} with DefaultAggregateService", "eventA", EventA.class);
        verify(logger).info("Registering event {}, {} with DefaultAggregateService", "eventB", EventB.class);
        verify(logger).trace("Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
        verify(logger).trace("SnapshotAwareAggregateService Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
    }

    @Test
    public void shouldCreateAggregateFromStreamOfThreeWithAFilteredOutSystemEvent() throws AggregateChangeDetectedException {
        final Optional<VersionedAggregate<TestAggregate>> versionedAggregate = Optional.empty();

        JsonObject eventPayloadA = mock(JsonObject.class);
        JsonObject eventPayloadB = mock(JsonObject.class);

        EventA eventA = mock(EventA.class);
        EventB eventB = mock(EventB.class);

        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(jsonObjectToObjectConverter.convert(eventPayloadB, EventB.class)).thenReturn(eventB);
        when(eventStream.getId()).thenReturn(STREAM_ID);
        when(eventStream.read()).thenReturn(Stream.of(
                envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA),
                envelopeFrom(metadataWithRandomUUID("eventB"), eventPayloadB),
                envelopeFrom(metadataWithRandomUUID("system.events.eventC"), eventPayloadB)));

        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(versionedAggregate);

        when(eventStream.getId()).thenReturn(STREAM_ID);

        registerEvent(EventA.class, "eventA");
        registerEvent(EventB.class, "eventB");

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
    public void shouldThrowExceptionForUnregisteredEvent() throws AggregateChangeDetectedException {
        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        final JsonObject eventPayloadA = mock(JsonObject.class);
        final EventA eventA = mock(EventA.class);

        when(eventStream.getId()).thenReturn(STREAM_ID);
        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(Optional.of(new VersionedAggregate<>(INITIAL_AGGREGATE_VERSION, new TestAggregate())));
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.readFrom(NEXT_AGGREGATE_VERSION)).thenReturn(of(envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA)));

        aggregateService.get(eventStream, TestAggregate.class);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForNonInstantiatableEvent() throws AggregateChangeDetectedException {
        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        final JsonObject eventPayloadA = mock(JsonObject.class);
        final EventA eventA = mock(EventA.class);

        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(Optional.of(new VersionedAggregate<>(INITIAL_AGGREGATE_VERSION, new TestAggregate())));
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.readFrom(NEXT_AGGREGATE_VERSION)).thenReturn(of(envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA)));

        registerEvent(EventA.class, "eventA");

        aggregateService.get(eventStream, PrivateAggregate.class);
    }

    @Test
    public void shouldReplayDeltaOfEventsOnAggregate() throws AggregateChangeDetectedException {
        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;

        final UUID streamId = UUID.randomUUID();
        final long currentStreamVersion = 5L;
        final TestAggregate aggregate = new TestAggregate();
        final long snapshotVersion = 2L;

        final JsonEnvelope jsonEventA = envelope().with(metadataWithRandomUUID("eventA")).withPayloadOf("value1", "name1").build();
        final JsonEnvelope jsonEventB = envelope().with(metadataWithRandomUUID("eventB")).withPayloadOf("value2", "name1").build();
        final JsonEnvelope jsonEventC = envelope().with(metadataWithRandomUUID("eventC")).withPayloadOf("value3", "name1").build();

        registerEvent(EventA.class, "eventA");
        registerEvent(EventB.class, "eventB");
        registerEvent(EventC.class, "eventC");

        final EventA eventA = new EventA("A1");
        final EventB eventB = new EventB("B1");
        final EventC eventC = new EventC("C1");

        when(eventStream.getId()).thenReturn(streamId);
        when(eventStream.getCurrentVersion()).thenReturn(currentStreamVersion);
        when(snapshotService.getLatestVersionedAggregate(streamId, TestAggregate.class)).thenReturn(
                Optional.of(new VersionedAggregate<>(snapshotVersion, aggregate)));
        when(eventStream.readFrom(snapshotVersion + 1)).thenReturn(of(jsonEventA, jsonEventB, jsonEventC));


        when(jsonObjectToObjectConverter.convert(jsonEventA.payloadAsJsonObject(), EventA.class)).thenReturn(eventA);
        when(jsonObjectToObjectConverter.convert(jsonEventB.payloadAsJsonObject(), EventB.class)).thenReturn(eventB);
        when(jsonObjectToObjectConverter.convert(jsonEventC.payloadAsJsonObject(), EventC.class)).thenReturn(eventC);

        aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate.recordedEvents(), hasItems(eventA, eventB, eventC));
    }

    @Test
    public void shouldRebuildAggregateOnModelChange() throws AggregateChangeDetectedException {
        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;

        final UUID streamId = UUID.randomUUID();
        final long currentStreamVersion = 3L;

        final JsonEnvelope jsonEventA = envelope().with(metadataWithRandomUUID("eventA")).withPayloadOf("value1", "name1").build();
        final JsonEnvelope jsonEventB = envelope().with(metadataWithRandomUUID("eventB")).withPayloadOf("value2", "name1").build();
        final JsonEnvelope jsonEventC = envelope().with(metadataWithRandomUUID("eventC")).withPayloadOf("value3", "name1").build();

        registerEvent(EventA.class, "eventA");
        registerEvent(EventB.class, "eventB");
        registerEvent(EventC.class, "eventC");

        final EventA eventA = new EventA("A1");
        final EventB eventB = new EventB("B1");
        final EventC eventC = new EventC("C1");

        when(eventStream.getId()).thenReturn(streamId);
        when(eventStream.getCurrentVersion()).thenReturn(currentStreamVersion);

        doThrow(new AggregateChangeDetectedException("Aggregate Change Detected")).when(snapshotService).getLatestVersionedAggregate(streamId, TestAggregate.class);

        when(jsonObjectToObjectConverter.convert(jsonEventA.payloadAsJsonObject(), EventA.class)).thenReturn(eventA);
        when(jsonObjectToObjectConverter.convert(jsonEventB.payloadAsJsonObject(), EventB.class)).thenReturn(eventB);
        when(jsonObjectToObjectConverter.convert(jsonEventC.payloadAsJsonObject(), EventC.class)).thenReturn(eventC);
        when(eventStream.read()).thenReturn(of(jsonEventA, jsonEventB, jsonEventC));

        final TestAggregate aggregate1 = aggregateService.get(eventStream, TestAggregate.class);

        verify(snapshotService).removeAllSnapshots(streamId, TestAggregate.class);

        assertThat(aggregate1.recordedEvents(), hasItems(eventA, eventB, eventC));
    }
}
