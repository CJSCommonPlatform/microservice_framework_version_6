package uk.gov.justice.services.core.aggregate;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.domain.snapshot.AggregateChangeDetectedException;
import uk.gov.justice.domain.snapshot.DefaultObjectInputStreamStrategy;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.VersionedAggregate;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.List;
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
 * Unit tests for the {@link AggregateService} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class AggregateServiceTest {

    @Mock
    private Logger logger;

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Mock
    private EventStream eventStream;

    @Mock
    private SnapshotService snapshotService;


    @InjectMocks
    private AggregateService aggregateService;

    private void registerEvent(Class clazz, String name) {
        aggregateService.register(new EventFoundEvent(clazz, name));
    }

    @Mock
    private DefaultObjectInputStreamStrategy streamStrategy;

    @Test
    public void shouldAttemptStoringSnapshot() throws AggregateChangeDetectedException {

        final UUID streamId = UUID.randomUUID();
        final long currentStreamVersion = 3l;
        final TestAggregate aggregate = new TestAggregate();
        final long snapshotVersion = 1l;

        when(eventStream.getId()).thenReturn(streamId);
        when(eventStream.getCurrentVersion()).thenReturn(currentStreamVersion);
        when(snapshotService.getLatestVersionedAggregate(streamId, TestAggregate.class, streamStrategy)).thenReturn(
                new VersionedAggregate<TestAggregate>(snapshotVersion, aggregate));
        when(eventStream.readFrom(snapshotVersion)).thenReturn(Stream.empty());

        aggregateService.get(eventStream, TestAggregate.class);

        verify(snapshotService).attemptAggregateStore(streamId, currentStreamVersion, aggregate, snapshotVersion);
    }


    @Test
    public void shouldReplayDeltaOfEventsOnAggregate() throws AggregateChangeDetectedException {
        final UUID streamId = UUID.randomUUID();
        final long currentStreamVersion = 5l;
        final TestAggregate aggregate = new TestAggregate();
        final long snapshotVersion = 2l;

        final JsonEnvelope jsonEventA = envelope().with(metadataWithRandomUUID("eventA")).withPayloadOf("value1", "name1").build();
        final JsonEnvelope jsonEventB = envelope().with(metadataWithRandomUUID("eventB")).withPayloadOf("value2", "name1").build();
        final JsonEnvelope jsonEventC = envelope().with(metadataWithRandomUUID("eventC")).withPayloadOf("value3", "name1").build();

        registerEvent(EventA.class, "eventA");
        registerEvent(EventB.class, "eventB");
        registerEvent(EventC.class, "eventC");

        final EventA eventA = new EventA();
        final EventB eventB = new EventB();
        final EventC eventC = new EventC();

        when(eventStream.getId()).thenReturn(streamId);
        when(eventStream.getCurrentVersion()).thenReturn(currentStreamVersion);
        when(snapshotService.getLatestVersionedAggregate(streamId, TestAggregate.class, streamStrategy)).thenReturn(
                new VersionedAggregate<TestAggregate>(snapshotVersion, aggregate));
        when(eventStream.readFrom(snapshotVersion)).thenReturn(Stream.of(jsonEventA, jsonEventB, jsonEventC));


        when(jsonObjectToObjectConverter.convert(jsonEventA.payloadAsJsonObject(), EventA.class)).thenReturn(eventA);
        when(jsonObjectToObjectConverter.convert(jsonEventB.payloadAsJsonObject(), EventB.class)).thenReturn(eventB);
        when(jsonObjectToObjectConverter.convert(jsonEventC.payloadAsJsonObject(), EventC.class)).thenReturn(eventC);

        aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate.repliedEvents, hasItems(eventA, eventB, eventC));
    }

    @Test
    public void shouldRebuildAggregateOnModelChange() throws AggregateChangeDetectedException {
        final UUID streamId = UUID.randomUUID();
        final long currentStreamVersion = 3l;
        final TestAggregate aggregate = new TestAggregate();
        final long snapshotVersion = 1l;

        final JsonEnvelope jsonEventA = envelope().with(metadataWithRandomUUID("eventA")).withPayloadOf("value1", "name1").build();
        final JsonEnvelope jsonEventB = envelope().with(metadataWithRandomUUID("eventB")).withPayloadOf("value2", "name1").build();
        final JsonEnvelope jsonEventC = envelope().with(metadataWithRandomUUID("eventC")).withPayloadOf("value3", "name1").build();

        registerEvent(EventA.class, "eventA");
        registerEvent(EventB.class, "eventB");
        registerEvent(EventC.class, "eventC");

        final EventA eventA = new EventA();
        final EventB eventB = new EventB();
        final EventC eventC = new EventC();

        when(eventStream.getId()).thenReturn(streamId);
        when(eventStream.getCurrentVersion()).thenReturn(currentStreamVersion);
        when(snapshotService.getLatestVersionedAggregate(streamId, TestAggregate.class, streamStrategy)).thenReturn(
                new VersionedAggregate<TestAggregate>(snapshotVersion, aggregate)).thenThrow(new AggregateChangeDetectedException("Aggregate Change Detected"));
        when(eventStream.readFrom(snapshotVersion)).thenReturn(Stream.empty());
        when(eventStream.read()).thenReturn(Stream.of(jsonEventA, jsonEventB, jsonEventC));
        when(snapshotService.getNewVersionedAggregate(TestAggregate.class)).thenReturn(
                new VersionedAggregate<TestAggregate>(0l, aggregate));
        when(snapshotService.rebuildAggregate(streamId,TestAggregate.class)).thenReturn(
                new VersionedAggregate<TestAggregate>(0l, aggregate));

        aggregateService.get(eventStream, TestAggregate.class);

        verify(snapshotService).attemptAggregateStore(streamId, currentStreamVersion,  aggregate, snapshotVersion);

        when(jsonObjectToObjectConverter.convert(jsonEventA.payloadAsJsonObject(), EventA.class)).thenReturn(eventA);
        when(jsonObjectToObjectConverter.convert(jsonEventB.payloadAsJsonObject(), EventB.class)).thenReturn(eventB);
        when(jsonObjectToObjectConverter.convert(jsonEventC.payloadAsJsonObject(), EventC.class)).thenReturn(eventC);

        aggregateService.get(eventStream, TestAggregate.class);

        verify(snapshotService).rebuildAggregate(streamId,TestAggregate.class);

        assertThat(aggregate.repliedEvents, hasItems(eventA, eventB, eventC));
    }

    @Event("eventA")
    public static class EventA {

    }

    @Event("eventB")
    public static class EventB {

    }

    @Event("eventC")
    public static class EventC {

    }

    private class TestAggregate implements Aggregate {
        private static final long serialVersionUID = 42L;

        public List<Object> repliedEvents = new ArrayList<>();

        @Override
        public Object apply(Object event) {
            repliedEvents.add(event);
            return event;
        }
    }
}
