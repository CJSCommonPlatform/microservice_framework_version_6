package uk.gov.justice.services.eventsourcing.source.api.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Position.first;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Position.head;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Position.positionOf;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntry;

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

    private static final String METADATA_JSON = "{\"field\": \"Value\"}";

    @Mock
    private EventJdbcRepository repository;

    @InjectMocks
    private EventsService service;

    @Test
    public void shouldReturnHeadEvents() throws Exception {

        final UUID streamId = randomUUID();

        final long pageSize = 2L;

        final Stream.Builder<EventEntry> eventEntryBuilder = Stream.builder();
        final Stream.Builder<Event> eventBuilder = Stream.builder();

        final ZonedDateTime event1CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payload1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payload2 = createObjectBuilder().add("field2", "value2").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payload1.toString(), event1CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payload2.toString(), event2CreatedAt);

        final EventEntry eventEntry1 = new EventEntry(randomUUID(), streamId, 1L, "Test Name1", payload1, event1CreatedAt.toString());
        final EventEntry eventEntry2 = new EventEntry(randomUUID(), streamId, 2L, "Test Name2", payload2, event2CreatedAt.toString());

        eventEntryBuilder.add(eventEntry1);
        eventEntryBuilder.add(eventEntry2);

        eventBuilder.add(event1);
        eventBuilder.add(event2);

        when(repository.head(streamId,pageSize)).thenReturn(eventBuilder.build());

        final List<EventEntry> entries = service.events(streamId, head(), BACKWARD, pageSize);

        assertThat(entries, hasSize(2));

        assertThat(entries.get(0).getStreamId(), is(streamId.toString()));

        assertThat(entries.get(0).getName(), is("Test Name1"));

        assertThat(entries.get(0).getSequenceId(), is(1L));

        assertThat(entries.get(0).getCreatedAt(), is(event1CreatedAt.toString()));

        assertThat(entries.get(0).getPayload(), is(notNullValue()));

        assertThat(entries.get(0).getPayload(), is(payload1));

        assertThat(entries.get(1).getStreamId(), is(streamId.toString()));

        assertThat(entries.get(1).getName(), is("Test Name2"));

        assertThat(entries.get(1).getSequenceId(), is(2L));

        assertThat(entries.get(1).getPayload(), is(payload2));

        assertThat(entries.get(1).getCreatedAt(), is(event2CreatedAt.toString()));

    }

    @Test
    public void shouldReturnFirstEvents() throws Exception {

        final UUID streamId = randomUUID();

        final Stream.Builder<EventEntry> eventEntryBuilder = Stream.builder();
        final Stream.Builder<Event> eventBuilder = Stream.builder();

        final ZonedDateTime event1CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payload1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payload2 = createObjectBuilder().add("field2", "value2").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payload1.toString(), event1CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payload2.toString(), event2CreatedAt);

        final EventEntry eventEntry1 = new EventEntry(randomUUID(), streamId, 1L, "Test Name1", payload1, event1CreatedAt.toString());
        final EventEntry eventEntry2 = new EventEntry(randomUUID(), streamId, 2L, "Test Name2", payload2, event2CreatedAt.toString());

        eventEntryBuilder.add(eventEntry1);
        eventEntryBuilder.add(eventEntry2);

        eventBuilder.add(event2);
        eventBuilder.add(event1);

        final long pageSize = 2L;

        when(repository.first(streamId,pageSize)).thenReturn(eventBuilder.build());

        final List<EventEntry> eventEntries = service.events(streamId, first(), FORWARD, pageSize);

        assertThat(eventEntries, hasSize(2));

        assertThat(eventEntries.get(0).getStreamId(), is(streamId.toString()));

        assertThat(eventEntries.get(0).getName(), is("Test Name2"));

        assertThat(eventEntries.get(0).getSequenceId(), is(2L));

        assertThat(eventEntries.get(0).getCreatedAt(), is(event2CreatedAt.toString()));

        assertThat(eventEntries.get(0).getPayload(), is(notNullValue()));

        assertThat(eventEntries.get(0).getPayload(), is(payload2));

        assertThat(eventEntries.get(1).getStreamId(), is(streamId.toString()));

        assertThat(eventEntries.get(1).getName(), is("Test Name1"));

        assertThat(eventEntries.get(1).getSequenceId(), is(1L));

        assertThat(eventEntries.get(1).getPayload(), is(payload1));

        assertThat(eventEntries.get(1).getCreatedAt(), is(event1CreatedAt.toString()));
    }

    @Test
    public void shouldReturnNextEvents() throws Exception {

        final UUID streamId = randomUUID();

        final Stream.Builder<EventEntry> eventEntryBuilder = Stream.builder();
        final Stream.Builder<Event> eventBuilder = Stream.builder();

        final ZonedDateTime event1CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payload1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payload2 = createObjectBuilder().add("field2", "value2").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payload1.toString(), event1CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payload2.toString(), event2CreatedAt);

        final EventEntry eventEntry1 = new EventEntry(randomUUID(), streamId, 1L, "Test Name1", payload1, event1CreatedAt.toString());
        final EventEntry eventEntry2 = new EventEntry(randomUUID(), streamId, 2L, "Test Name2", payload2, event2CreatedAt.toString());

        eventEntryBuilder.add(eventEntry1);
        eventEntryBuilder.add(eventEntry2);

        eventBuilder.add(event2);
        eventBuilder.add(event1);

        final long sequenceId = 1L;

        final long pageSize = 2L;

        when(repository.next(streamId,sequenceId, pageSize)).thenReturn(eventBuilder.build());

        final List<EventEntry> eventEntries = service.events(streamId, positionOf(sequenceId), BACKWARD, pageSize);

        assertThat(eventEntries, hasSize(2));

        assertThat(eventEntries.get(0).getStreamId(), is(streamId.toString()));

        assertThat(eventEntries.get(0).getName(), is("Test Name2"));

        assertThat(eventEntries.get(0).getSequenceId(), is(2L));

        assertThat(eventEntries.get(0).getCreatedAt(), is(event2CreatedAt.toString()));

        assertThat(eventEntries.get(0).getPayload(), is(notNullValue()));

        assertThat(eventEntries.get(0).getPayload(), is(payload2));

        assertThat(eventEntries.get(1).getStreamId(), is(streamId.toString()));

        assertThat(eventEntries.get(1).getName(), is("Test Name1"));

        assertThat(eventEntries.get(1).getSequenceId(), is(1L));

        assertThat(eventEntries.get(1).getPayload(), is(payload1));

        assertThat(eventEntries.get(1).getCreatedAt(), is(event1CreatedAt.toString()));
    }

    @Test
    public void shouldReturnPreviousEvents() throws Exception {

        final UUID streamId = randomUUID();

        final Stream.Builder<EventEntry> eventEntryBuilder = Stream.builder();
        final Stream.Builder<Event> eventBuilder = Stream.builder();

        final ZonedDateTime event3CreatedAt = new UtcClock().now();
        final ZonedDateTime event4CreatedAt = new UtcClock().now();

        final JsonObject payload3 = createObjectBuilder().add("field3", "value3").build();
        final JsonObject payload4 = createObjectBuilder().add("field4", "value4").build();

        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payload3.toString(), event3CreatedAt);
        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payload4.toString(), event4CreatedAt);

        final EventEntry eventEntry3 = new EventEntry(randomUUID(), streamId, 3L, "Test Name3", payload3, event3CreatedAt.toString());
        final EventEntry eventEntry4 = new EventEntry(randomUUID(), streamId, 4L, "Test Name4", payload4, event4CreatedAt.toString());

        eventEntryBuilder.add(eventEntry3);
        eventEntryBuilder.add(eventEntry4);

        eventBuilder.add(event4);
        eventBuilder.add(event3);

        final long sequenceId = 3L;

        when(repository.previous(streamId,sequenceId, 2L)).thenReturn(eventBuilder.build());

        final List<EventEntry> eventEntries = service.events(streamId, positionOf(sequenceId), FORWARD, 2L);

        assertThat(eventEntries, hasSize(2));

        assertThat(eventEntries.get(0).getStreamId(), is(streamId.toString()));

        assertThat(eventEntries.get(0).getName(), is("Test Name4"));

        assertThat(eventEntries.get(0).getSequenceId(), is(4L));

        assertThat(eventEntries.get(0).getCreatedAt(), is(event4CreatedAt.toString()));

        assertThat(eventEntries.get(0).getPayload(), is(notNullValue()));

        assertThat(eventEntries.get(0).getPayload(), is(payload4));

        assertThat(eventEntries.get(1).getStreamId(), is(streamId.toString()));

        assertThat(eventEntries.get(1).getName(), is("Test Name3"));

        assertThat(eventEntries.get(1).getSequenceId(), is(3L));

        assertThat(eventEntries.get(1).getPayload(), is(payload3));

        assertThat(eventEntries.get(1).getCreatedAt(), is(event3CreatedAt.toString()));
    }
}