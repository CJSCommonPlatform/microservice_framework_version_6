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
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.domain.aggregate.PrivateAggregate;
import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.domain.aggregate.event.EventA;
import uk.gov.justice.domain.aggregate.event.EventB;
import uk.gov.justice.domain.aggregate.event.EventC;
import uk.gov.justice.domain.snapshot.AggregateChangeDetectedException;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.VersionedAggregate;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Unit tests for the {@link SnapshotAwareAggregateService} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class SnapshotAwareAggregateServiceTest {

    private static final UUID STREAM_ID = randomUUID();

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

    private static final long INITIAL_AGGREGATE_VERSION = 0L;
    private static final long NEXT_AGGREGATE_VERSION = 1L;

    private void registerEvent(Class clazz, String name) {
        defaultAggregateService.register(new EventFoundEvent(clazz, name));
    }

    @Test
    public void shouldCreateAggregateFromEmptyStream() throws AggregateChangeDetectedException {
        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;

        when(eventStream.getId()).thenReturn(STREAM_ID);
        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(Optional.of(new VersionedAggregate<>(INITIAL_AGGREGATE_VERSION, new TestAggregate())));
        when(eventStream.readFrom(NEXT_AGGREGATE_VERSION)).thenReturn(Stream.empty());
        TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), empty());
        verify(logger).trace("SnapshotAwareAggregateService Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
    }

    @Test
    public void shouldCreateAggregateFromStreamWithOneEvent() throws AggregateChangeDetectedException {
        Optional<VersionedAggregate<TestAggregate>> versionedAggregate = Optional.empty();
        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        EventA eventA = new EventA();
        JsonObject eventPayloadA = mock(JsonObject.class);

        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.getId()).thenReturn(STREAM_ID);
        when(eventStream.read()).thenReturn(of(
                envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA)));
        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(versionedAggregate);
        registerEvent(EventA.class, "eventA");
        registerEvent(EventB.class, "eventB");

        TestAggregate aggregateActual = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregateActual, notNullValue());
        assertThat(aggregateActual.recordedEvents(), hasSize(1));
        assertThat(aggregateActual.recordedEvents(), hasItems(eventA));
        verify(logger).info("Registering event {}, {} with DefaultAggregateService", "eventA", EventA.class);
        verify(logger).trace("Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
        verify(logger).trace("SnapshotAwareAggregateService Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);
    }

    @Test
    public void shouldCreateAggregateFromStreamWithTwoEvents() throws AggregateChangeDetectedException {

        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        EventA eventA = new EventA();
        EventB eventB = mock(EventB.class);
        JsonObject eventPayloadA = mock(JsonObject.class);
        JsonObject eventPayloadB = mock(JsonObject.class);

        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(jsonObjectToObjectConverter.convert(eventPayloadB, EventB.class)).thenReturn(eventB);
        when(eventStream.getId()).thenReturn(STREAM_ID);
        when(eventStream.read()).thenReturn(of(
                envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA), envelopeFrom(metadataWithRandomUUID("eventB"), eventPayloadB)));

        Optional<VersionedAggregate<TestAggregate>> versionedAggregate = Optional.empty();
        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(versionedAggregate);
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
        verify(logger).trace("SnapshotAwareAggregateService Recreating aggregate for instance {} of aggregate type {}", STREAM_ID, TestAggregate.class);


    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForUnregisteredEvent() throws AggregateChangeDetectedException {
        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;

        when(eventStream.getId()).thenReturn(STREAM_ID);
        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(Optional.of(new VersionedAggregate<>(INITIAL_AGGREGATE_VERSION, new TestAggregate())));

        JsonObject eventPayloadA = mock(JsonObject.class);
        EventA eventA = mock(EventA.class);
        when(jsonObjectToObjectConverter.convert(eventPayloadA, EventA.class)).thenReturn(eventA);
        when(eventStream.readFrom(NEXT_AGGREGATE_VERSION)).thenReturn(of(envelopeFrom(metadataWithRandomUUID("eventA"), eventPayloadA)));
        aggregateService.get(eventStream, TestAggregate.class);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForNonInstantiatableEvent() throws AggregateChangeDetectedException {
        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;

        when(snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class)).thenReturn(Optional.of(new VersionedAggregate<>(INITIAL_AGGREGATE_VERSION, new TestAggregate())));
        JsonObject eventPayloadA = mock(JsonObject.class);
        EventA eventA = mock(EventA.class);
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
        final long currentStreamVersion = 5l;
        final TestAggregate aggregate = new TestAggregate();
        final long snapshotVersion = 2l;

        final JsonEnvelope jsonEventA = DefaultJsonEnvelope.envelope().with(JsonObjectMetadata.metadataWithRandomUUID("eventA")).withPayloadOf("value1", "name1").build();
        final JsonEnvelope jsonEventB = DefaultJsonEnvelope.envelope().with(JsonObjectMetadata.metadataWithRandomUUID("eventB")).withPayloadOf("value2", "name1").build();
        final JsonEnvelope jsonEventC = DefaultJsonEnvelope.envelope().with(JsonObjectMetadata.metadataWithRandomUUID("eventC")).withPayloadOf("value3", "name1").build();

        registerEvent(EventA.class, "eventA");
        registerEvent(EventB.class, "eventB");
        registerEvent(EventC.class, "eventC");

        final EventA eventA = new EventA("A1");
        final EventB eventB = new EventB("B1");
        final EventC eventC = new EventC("C1");

        Mockito.when(eventStream.getId()).thenReturn(streamId);
        Mockito.when(eventStream.getCurrentVersion()).thenReturn(currentStreamVersion);
        Mockito.when(snapshotService.getLatestVersionedAggregate(streamId, TestAggregate.class)).thenReturn(
                Optional.of(new VersionedAggregate<>(snapshotVersion, aggregate)));
        Mockito.when(eventStream.readFrom(snapshotVersion + 1)).thenReturn(of(jsonEventA, jsonEventB, jsonEventC));


        Mockito.when(jsonObjectToObjectConverter.convert(jsonEventA.payloadAsJsonObject(), EventA.class)).thenReturn(eventA);
        Mockito.when(jsonObjectToObjectConverter.convert(jsonEventB.payloadAsJsonObject(), EventB.class)).thenReturn(eventB);
        Mockito.when(jsonObjectToObjectConverter.convert(jsonEventC.payloadAsJsonObject(), EventC.class)).thenReturn(eventC);

        aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate.recordedEvents(), Matchers.hasItems(eventA, eventB, eventC));
    }

    @Test
    public void shouldRebuildAggregateOnModelChange() throws AggregateChangeDetectedException {
        defaultAggregateService.logger = logger;
        defaultAggregateService.jsonObjectToObjectConverter = jsonObjectToObjectConverter;

        final UUID streamId = UUID.randomUUID();
        final long currentStreamVersion = 3l;
        final TestAggregate aggregate = new TestAggregate();

        final JsonEnvelope jsonEventA = DefaultJsonEnvelope.envelope().with(JsonObjectMetadata.metadataWithRandomUUID("eventA")).withPayloadOf("value1", "name1").build();
        final JsonEnvelope jsonEventB = DefaultJsonEnvelope.envelope().with(JsonObjectMetadata.metadataWithRandomUUID("eventB")).withPayloadOf("value2", "name1").build();
        final JsonEnvelope jsonEventC = DefaultJsonEnvelope.envelope().with(JsonObjectMetadata.metadataWithRandomUUID("eventC")).withPayloadOf("value3", "name1").build();

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

        TestAggregate aggregate1 = aggregateService.get(eventStream, TestAggregate.class);

        Mockito.verify(snapshotService).removeAllSnapshots(streamId, TestAggregate.class);

        assertThat(aggregate1.recordedEvents(), Matchers.hasItems(eventA, eventB, eventC));
    }
}
