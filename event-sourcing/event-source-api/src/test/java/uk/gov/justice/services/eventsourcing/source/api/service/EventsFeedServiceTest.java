package uk.gov.justice.services.eventsourcing.source.api.service;

import static java.net.URI.create;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jdbc.persistence.Link.HEAD;
import static uk.gov.justice.services.jdbc.persistence.Link.LAST;
import static uk.gov.justice.services.jdbc.persistence.Link.NEXT;
import static uk.gov.justice.services.jdbc.persistence.Link.PREVIOUS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.FeedGenerator;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Paging;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntry;
import uk.gov.justice.services.jdbc.persistence.Link;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventsFeedServiceTest {

    private static final String METADATA_JSON = "{\"field\": \"Value\"}";

    @Mock
    private EventJdbcRepository repository;

    @Mock
    private FeedGenerator<Event, EventEntry> feedGenerator;

    @InjectMocks
    private EventsFeedService service;

    @Test
    public void shouldReturnNextButNotPrevious() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:222/context/"), create("event-streams/" + streamId));

        final long pageSize = 2L;

        final Stream.Builder<EventEntry> eventEntryBuilder = Stream.builder();
        final Stream.Builder<Event> eventBuilder = Stream.builder();

        final ZonedDateTime event1CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payload1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payload2 = createObjectBuilder().add("field2", "value2").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payload1.toString(), event1CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payload2.toString(), event2CreatedAt);

        final EventEntry eventEntry1 = new EventEntry(randomUUID(), streamId, "Test Name1", 1L, payload1, event1CreatedAt.toString());
        final EventEntry eventEntry2 = new EventEntry(randomUUID(), streamId, "Test Name2", 2L, payload2, event2CreatedAt.toString());

        eventEntryBuilder.add(eventEntry1);
        eventEntryBuilder.add(eventEntry2);

        eventBuilder.add(event1);
        eventBuilder.add(event2);

        initialiseWithPageSize(service);

        final long offset = 0L;

        when(repository.recordExists(offset, params)).thenReturn(true);

        when(repository.getFeed(offset, HEAD, pageSize, params)).thenReturn(eventBuilder.build());

        final String nextUrl = "http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + NEXT
                + "/" + pageSize;
        final String headUrl = "http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + HEAD + "/" + pageSize;
        final String lastUrl = "http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + LAST + "/" + pageSize;

        final Paging paging = new Paging(null, nextUrl, headUrl, lastUrl);

        when(feedGenerator.feed(offset, HEAD, pageSize, uriInfo, params)).thenReturn(new Feed(eventEntryBuilder.build().collect(Collectors.toList()), paging));

        final Feed<EventEntry> feedActual = service.feed(offset, HEAD, pageSize, uriInfo, params);

        final List<EventEntry> streamData = feedActual.getData();

        final Paging pagingActual = feedActual.getPaging();

        assertThat(streamData, hasSize(2));

        assertThat(streamData.get(0).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(0).getName(), is("Test Name1"));

        assertThat(streamData.get(0).getSequenceId(), is(1L));

        assertThat(streamData.get(0).getCreatedAt(), is(event1CreatedAt.toString()));

        assertThat(streamData.get(0).getPayload(), is(notNullValue()));

        assertThat(streamData.get(0).getPayload(), is(payload1));

        assertThat(streamData.get(1).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(1).getName(), is("Test Name2"));

        assertThat(streamData.get(1).getSequenceId(), is(2L));

        assertThat(streamData.get(1).getPayload(), is(payload2));

        assertThat(streamData.get(1).getCreatedAt(), is(event2CreatedAt.toString()));

        assertThat(pagingActual.getPrevious(), is(nullValue()));

        assertThat(pagingActual.getNext(), is("http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + NEXT
                + "/" + pageSize));

        assertThat(pagingActual.getHead(), is("http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + HEAD + "/" + pageSize));

        assertThat(pagingActual.getLast(), is("http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + LAST + "/" + pageSize));
    }

    @Test
    public void shouldReturnPreviousButNotNext() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:222/context/"), create("event-streams/" + streamId));

        final Stream.Builder<EventEntry> eventEntryBuilder = Stream.builder();
        final Stream.Builder<Event> eventBuilder = Stream.builder();

        final ZonedDateTime event1CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();

        final JsonObject payload1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payload2 = createObjectBuilder().add("field2", "value2").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payload1.toString(), event1CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payload2.toString(), event2CreatedAt);

        final EventEntry eventEntry1 = new EventEntry(randomUUID(), streamId, "Test Name1", 1L, payload1, event1CreatedAt.toString());
        final EventEntry eventEntry2 = new EventEntry(randomUUID(), streamId, "Test Name2", 2L, payload2, event2CreatedAt.toString());

        eventEntryBuilder.add(eventEntry1);
        eventEntryBuilder.add(eventEntry2);

        eventBuilder.add(event2);
        eventBuilder.add(event1);

        initialiseWithPageSize(service);

        final long offset = 2L;

        final long pageSize = 2L;

        when(repository.recordExists(3L, params)).thenReturn(true);

        when(repository.getFeed(offset, PREVIOUS, pageSize, params)).thenReturn(eventBuilder.build());

        final String previousUrl = "http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + Link.PREVIOUS
                + "/" + pageSize;
        final String headUrl = "http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + HEAD + "/" + pageSize;
        final String lastUrl = "http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + LAST + "/" + pageSize;

        final Paging paging = new Paging(previousUrl, null, headUrl, lastUrl);

        when(feedGenerator.feed(offset, PREVIOUS, pageSize, uriInfo, params)).thenReturn(new Feed(eventEntryBuilder.build().collect(Collectors.toList()), paging));

        final Feed<EventEntry> feedActual = service.feed(offset, PREVIOUS, pageSize, uriInfo, params);

        final List<EventEntry> streamData = feedActual.getData();

        final Paging pagingActual = feedActual.getPaging();

        assertThat(streamData, hasSize(2));

        assertThat(streamData.get(0).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(0).getName(), is("Test Name2"));

        assertThat(streamData.get(0).getSequenceId(), is(2L));

        assertThat(streamData.get(0).getCreatedAt(), is(event2CreatedAt.toString()));

        assertThat(streamData.get(0).getPayload(), is(notNullValue()));

        assertThat(streamData.get(0).getPayload(), is(payload2));

        assertThat(streamData.get(1).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(1).getName(), is("Test Name1"));

        assertThat(streamData.get(1).getSequenceId(), is(1L));

        assertThat(streamData.get(1).getPayload(), is(payload1));

        assertThat(streamData.get(1).getCreatedAt(), is(event1CreatedAt.toString()));

        assertThat(pagingActual.getPrevious(), is("http://server:222/context/event-streams/" + streamId + "/" + 3 + "/" + Link.PREVIOUS
                + "/" + pageSize));

        assertThat(pagingActual.getNext(), is(nullValue()));

        assertThat(pagingActual.getHead(), is("http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + HEAD + "/" + pageSize));

        assertThat(pagingActual.getLast(), is("http://server:222/context/event-streams/" + streamId + "/" + 0 + "/" + LAST + "/" + pageSize));
    }

    private void initialiseWithPageSize(final EventsFeedService service) {

        service.initialise();
    }
}