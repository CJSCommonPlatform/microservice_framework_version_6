package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.first;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.head;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.position;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EventsServiceTest {

    @Mock
    private EventSource eventSource;

    @InjectMocks
    private EventsService service;

    @Test
    public void shouldReturnHeadEvents() throws Exception {

        final UUID streamId = randomUUID();
        final UUID firstEventId = randomUUID();
        final UUID secondEventId = randomUUID();
        final ZonedDateTime event1CreatedAt = now();
        final ZonedDateTime event2CreatedAt = now();
        final long pageSize = 2L;

        final JsonObject payload1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payload2 = createObjectBuilder().add("field2", "value2").build();

        final JsonEnvelope event1 = envelope()
                .withPayloadOf("value1","field1")
                .with(metadataOf(firstEventId,"Test Name1")
                        .withVersion(1L)
                        .withStreamId(streamId)
                        .createdAt(event1CreatedAt)
                ).build();
        final JsonEnvelope event2 =  envelope()
                .withPayloadOf("value2","field2")
                .with(metadataOf(secondEventId,"Test Name2")
                        .withVersion(2L)
                        .withStreamId(streamId)
                        .createdAt(event2CreatedAt)
                ).build();

        final EventStream eventStream  = mock(EventStream.class);

        when(eventSource.getStreamById(streamId)).thenReturn(eventStream);
        when(eventStream.getPosition()).thenReturn(2L);
        when(eventStream.readFrom(eventStream.size() - 1)).thenReturn(Stream.of(event1,event2));

        final List<EventEntry> entries = service.events(streamId, head(), BACKWARD, pageSize);

        assertThat(entries, hasSize(2));

        assertThat(entries.get(0).getStreamId(), is(streamId.toString()));
        assertThat(entries.get(0).getName(), is("Test Name1"));
        assertThat(entries.get(0).getPosition(), is(1L));
        assertThat(entries.get(0).getPayload(), is(payload1));
        assertThat(entries.get(0).getCreatedAt(), is(ZonedDateTimes.toString(event1CreatedAt)));

        assertThat(entries.get(1).getStreamId(), is(streamId.toString()));
        assertThat(entries.get(1).getName(), is("Test Name2"));
        assertThat(entries.get(1).getPosition(), is(2L));
        assertThat(entries.get(1).getCreatedAt(), is(ZonedDateTimes.toString(event2CreatedAt)));
        assertThat(entries.get(1).getPayload(), is(payload2));
    }

    @Test
    public void shouldReturnFirstEvents() throws Exception {

        final UUID streamId = randomUUID();
        final ZonedDateTime event1CreatedAt = now();
        final ZonedDateTime event2CreatedAt = now();
        final long pageSize = 2L;

        final JsonObject payload1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payload2 = createObjectBuilder().add("field2", "value2").build();

        final JsonEnvelope event1 = envelope()
                .withPayloadOf("value1","field1")
                .with(metadataOf(streamId,"Test Name1")
                        .withVersion(1L)
                        .withStreamId(streamId)
                        .createdAt(event1CreatedAt)
                ).build();
        final JsonEnvelope event2 =  envelope()
                .withPayloadOf("value2","field2")
                .with(metadataOf(streamId,"Test Name2")
                        .withVersion(2L)
                        .withStreamId(streamId)
                        .createdAt(event2CreatedAt)
                ).build();

        final EventStream eventStream  = mock(EventStream.class);

        when(eventSource.getStreamById(streamId)).thenReturn(eventStream);
        when(eventStream.size()).thenReturn(2L);
        when(eventStream.readFrom(first().getPosition())).thenReturn(Stream.of(event1,event2));

        final List<EventEntry> eventEntries = service.events(streamId, first(), FORWARD, pageSize);

        assertThat(eventEntries, hasSize(2));

        assertThat(eventEntries.get(0).getStreamId(), is(streamId.toString()));
        assertThat(eventEntries.get(0).getName(), is("Test Name1"));
        assertThat(eventEntries.get(0).getPosition(), is(1L));
        assertThat(eventEntries.get(0).getPayload(), is(payload1));
        assertThat(eventEntries.get(0).getCreatedAt(), is(ZonedDateTimes.toString(event1CreatedAt)));

        assertThat(eventEntries.get(1).getStreamId(), is(streamId.toString()));
        assertThat(eventEntries.get(1).getName(), is("Test Name2"));
        assertThat(eventEntries.get(1).getPosition(), is(2L));
        assertThat(eventEntries.get(1).getCreatedAt(), is(ZonedDateTimes.toString(event2CreatedAt)));
        assertThat(eventEntries.get(1).getPayload(), is(payload2));
    }

    @Test
    public void shouldReturnPreviousEvents() throws Exception {

        final UUID streamId = randomUUID();
        final UUID firstEventId = randomUUID();
        final UUID secondEventId = randomUUID();
        final ZonedDateTime event2CreatedAt = now();
        final ZonedDateTime event3CreatedAt = now();
        final ZonedDateTime event4CreatedAt = now();
        final long pageSize = 2L;

        final JsonObject payload3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payload2 = createObjectBuilder().add("field2", "value2").build();

        final JsonEnvelope event2 =  envelope()
                .withPayloadOf("value2","field2")
                .with(metadataOf(secondEventId,"Test Name2")
                        .withVersion(2L)
                        .withStreamId(streamId)
                        .createdAt(event2CreatedAt)
                ).build();
        final JsonEnvelope event3 = envelope()
                .withPayloadOf("value3","field3")
                .with(metadataOf(firstEventId,"Test Name3")
                        .withVersion(3L)
                        .withStreamId(streamId)
                        .createdAt(event3CreatedAt)
                ).build();
        final JsonEnvelope event4 =  envelope()
                .withPayloadOf("value4","field4")
                .with(metadataOf(secondEventId,"Test Name4")
                        .withVersion(4L)
                        .withStreamId(streamId)
                        .createdAt(event4CreatedAt)
                ).build();

        final EventStream eventStream  = mock(EventStream.class);

        when(eventSource.getStreamById(streamId)).thenReturn(eventStream);

        when(eventStream.readFrom(2L)).thenReturn(Stream.of(event2, event3, event4));

        final List<EventEntry> eventEntries = service.events(streamId, position(3L), BACKWARD, pageSize);

        assertThat(eventEntries, hasSize(2));

        assertThat(eventEntries.get(0).getStreamId(), is(streamId.toString()));
        assertThat(eventEntries.get(0).getName(), is("Test Name2"));
        assertThat(eventEntries.get(0).getPosition(), is(2L));
        assertThat(eventEntries.get(0).getPayload(), is(payload2));
        assertThat(eventEntries.get(0).getCreatedAt(), is(ZonedDateTimes.toString(event2CreatedAt)));

        assertThat(eventEntries.get(1).getStreamId(), is(streamId.toString()));
        assertThat(eventEntries.get(1).getName(), is("Test Name3"));
        assertThat(eventEntries.get(1).getPosition(), is(3L));
        assertThat(eventEntries.get(1).getCreatedAt(), is(ZonedDateTimes.toString(event3CreatedAt)));
        assertThat(eventEntries.get(1).getPayload(), is(payload3));
    }

    @Test
    public void shouldReturnEmptyList(){
        final UUID streamId = randomUUID();
        final Position position = Position.empty();
        final List<EventEntry> eventEntries = service.events(streamId, position, null, 1);

        assertThat(eventEntries , is(empty()));
    }


    @Test
    public void shouldReturnNextEvents() throws Exception {

        final UUID streamId = randomUUID();
        final UUID firstEventId = randomUUID();
        final UUID secondEventId = randomUUID();
        final ZonedDateTime event3CreatedAt = now();
        final ZonedDateTime event4CreatedAt = now();

        final long pageSize = 2L;

        final JsonObject payload4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payload3 = createObjectBuilder().add("field3", "value3").build();

        final JsonEnvelope event4 =  envelope()
                .withPayloadOf("value4","field4")
                .with(metadataOf(secondEventId,"Test Name4")
                        .withVersion(4L)
                        .withStreamId(streamId)
                        .createdAt(event4CreatedAt)
                ).build();
        final JsonEnvelope event3 = envelope()
                .withPayloadOf("value3","field3")
                .with(metadataOf(firstEventId,"Test Name3")
                        .withVersion(3L)
                        .withStreamId(streamId)
                        .createdAt(event3CreatedAt)
                ).build();

        final EventStream eventStream  = mock(EventStream.class);

        final long positionId = 3L;

        when(eventSource.getStreamById(streamId)).thenReturn(eventStream);
        when(eventStream.readFrom(positionId)).thenReturn(Stream.of(event3 , event4));

        final List<EventEntry> eventEntries = service.events(streamId, position(positionId), FORWARD, pageSize);

        assertThat(eventEntries, hasSize(2));

        assertThat(eventEntries.get(0).getStreamId(), is(streamId.toString()));
        assertThat(eventEntries.get(0).getName(), is("Test Name3"));
        assertThat(eventEntries.get(0).getPosition(), is(3L));
        assertThat(eventEntries.get(0).getPayload(), is(payload3));
        assertThat(eventEntries.get(0).getCreatedAt(), is(ZonedDateTimes.toString(event3CreatedAt)));

        assertThat(eventEntries.get(1).getStreamId(), is(streamId.toString()));
        assertThat(eventEntries.get(1).getName(), is("Test Name4"));
        assertThat(eventEntries.get(1).getPosition(), is(4L));
        assertThat(eventEntries.get(1).getCreatedAt(), is(ZonedDateTimes.toString(event4CreatedAt)));
        assertThat(eventEntries.get(1).getPayload(), is(payload4));
    }

    @Test
    public void shouldReturnEventExist(){
        final UUID streamId = randomUUID();
        final long position = 1L;

        final ZonedDateTime createdAt = now();
        final JsonEnvelope event =  envelope()
                .withPayloadOf("value1","field1")
                .with(metadataOf(streamId,"Test Name1")
                        .withVersion(position)
                        .withStreamId(streamId)
                        .createdAt(createdAt)
                ).build();
        final EventStream eventStream = mock(EventStream.class);

        when(eventSource.getStreamById(streamId)).thenReturn(eventStream);
        when(eventStream.readFrom(1L)).thenReturn(Stream.of(event));

        assertTrue(service.eventExists(streamId , position));
        verify(eventSource).getStreamById(streamId);
        verify(eventStream).readFrom(position);
    }
}
