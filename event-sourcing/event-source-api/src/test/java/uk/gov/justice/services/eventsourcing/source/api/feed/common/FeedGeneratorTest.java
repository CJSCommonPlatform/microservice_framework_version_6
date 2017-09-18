package uk.gov.justice.services.eventsourcing.source.api.feed.common;

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
import uk.gov.justice.services.eventsourcing.source.api.feed.event.Event2FeedEntryMappingStrategy;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntry;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntryFeedMaxMinProviderProvider;
import uk.gov.justice.services.jdbc.persistence.Link;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeedGeneratorTest {

    private static final String METADATA_JSON = "{\"field\": \"Value\"}";

    @Mock
    private EventJdbcRepository repository;

    private Event2FeedEntryMappingStrategy event2FeedEntryMappingStrategy = new Event2FeedEntryMappingStrategy();

    private EventEntryFeedMaxMinProviderProvider eventEntryFeedMaxMinProvider = new EventEntryFeedMaxMinProviderProvider();

    private static ResteasyUriInfo uriInfoWithAbsoluteUri(final String absoluteUri) {
        return new ResteasyUriInfo(absoluteUri, "", "");
    }

    @Test
    public void shouldReturnEventsWhenLessRecordsThanPageSizeWhenLookingForNewerEvents() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(repository, event2FeedEntryMappingStrategy,
                eventEntryFeedMaxMinProvider);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final Stream.Builder<Event> builder = Stream.builder();

        final Event event1 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());

        builder.add(event1);

        when(repository.recordExists(4L, params)).thenReturn(true);

        when(repository.recordExists(2L, params)).thenReturn(false);

        when(repository.getFeed(3L, PREVIOUS, 2L, params)).thenReturn(builder.build());

        final Feed<EventEntry> feedActual = feedGenerator.feed(3L, PREVIOUS, 2L, uriInfo, params);

        final List<EventEntry> feed = feedActual.getData();

        final Paging paging = feedActual.getPaging();

        assertThat(feed, hasSize(1));

        assertThat(feed.get(0).getStreamId(), is(streamId.toString()));

        assertThat(feed.get(0).getSequenceId(), is(3L));

        assertThat(feed.get(0).getPayload(), is(notNullValue()));

        assertThat(paging.getNext(), is(nullValue()));

        assertThat(paging.getPrevious(), is("http://server:123/context/event-streams/" + streamId + "/" + 4 + "/" + Link.PREVIOUS
                + "/" + 2));

        assertThat(paging.getHead(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.HEAD + "/" + 2L));

        assertThat(paging.getLast(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.LAST + "/" + 2L));


    }

    @Test
    public void shouldReturnEventsWhenLessRecordsThanPageSizeWhenLookingForOlderEvents() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(repository, event2FeedEntryMappingStrategy,
                eventEntryFeedMaxMinProvider);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final Stream.Builder<Event> builder = Stream.builder();

        final Event event1 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());

        builder.add(event1);

        when(repository.recordExists(4L, params)).thenReturn(false);

        when(repository.recordExists(2L, params)).thenReturn(true);

        when(repository.getFeed(3L, NEXT, 2L, params)).thenReturn(builder.build());

        final Feed<EventEntry> feedActual = feedGenerator.feed(3L, NEXT, 2L, uriInfo, params);

        final List<EventEntry> feed = feedActual.getData();

        final Paging paging = feedActual.getPaging();

        assertThat(feed, hasSize(1));

        assertThat(feed.get(0).getStreamId(), is(streamId.toString()));

        assertThat(feed.get(0).getSequenceId(), is(3L));

        assertThat(feed.get(0).getPayload(), is(notNullValue()));

        assertThat(paging.getPrevious(), is(nullValue()));

        assertThat(paging.getNext(), is("http://server:123/context/event-streams/" + streamId + "/" + 2 + "/" + Link.NEXT
                + "/" + 2L));

        assertThat(paging.getHead(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.HEAD + "/" + 2L));

        assertThat(paging.getLast(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.LAST + "/" + 2L));
    }

    @Test
    public void shouldReturnFeedWhenSameNumberOfRecordsAsPageSizeWhenLookingForNewerEvents() throws Exception {
        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final Stream.Builder<Event> builder = Stream.builder();

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());
        builder.add(event2);

        builder.add(event1);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(repository, event2FeedEntryMappingStrategy,
                eventEntryFeedMaxMinProvider);

        when(repository.recordExists(3L, params)).thenReturn(true);

        when(repository.recordExists(0L, params)).thenReturn(false);

        when(repository.getFeed(3L, PREVIOUS, 2L, params)).thenReturn(builder.build());

        final Feed<EventEntry> feedActual = feedGenerator.feed(3L, PREVIOUS, 2L, uriInfo, params);

        final List<EventEntry> feed = feedActual.getData();

        final Paging paging = feedActual.getPaging();

        assertThat(feed, hasSize(2));

        assertThat(feed.get(0).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(0).getSequenceId(), is(2L));
        assertThat(feed.get(0).getPayload(), is(payloadEvent2));
        assertThat(feed.get(1).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(1).getSequenceId(), is(1L));
        assertThat(feed.get(1).getPayload(), is(payloadEvent1));

        assertThat(paging.getNext(), is(nullValue()));

        assertThat(paging.getPrevious(), is("http://server:123/context/event-streams/" + streamId + "/" + 3 + "/" + Link.PREVIOUS
                + "/" + 2));

        assertThat(paging.getHead(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.HEAD + "/" + 2L));

        assertThat(paging.getLast(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.LAST + "/" + 2L));

    }


    @Test
    public void shouldReturnFeedWhenSameNumberOfRecordsAsPageSizeWhenLookingForOlderEvents() throws Exception {
        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);


        final Stream.Builder<Event> builder = Stream.builder();

        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();

        final Event event4 = new Event(randomUUID(), streamId, 4L, "Test Name4", METADATA_JSON, payloadEvent4.toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), new UtcClock().now());

        builder.add(event4);
        builder.add(event3);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(repository, event2FeedEntryMappingStrategy,
                eventEntryFeedMaxMinProvider);


        when(repository.recordExists(5L, params)).thenReturn(false);

        when(repository.recordExists(2L, params)).thenReturn(true);

        when(repository.getFeed(4L, NEXT, 2L, params)).thenReturn(builder.build());

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final Feed<EventEntry> feedActual = feedGenerator.feed(4L, NEXT, 2L, uriInfo, params);

        final List<EventEntry> feed = feedActual.getData();

        final Paging paging = feedActual.getPaging();

        assertThat(feed, hasSize(2));
        assertThat(feed.get(0).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(0).getSequenceId(), is(4L));
        assertThat(feed.get(0).getPayload(), is(payloadEvent4));
        assertThat(feed.get(1).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(1).getSequenceId(), is(3L));
        assertThat(feed.get(1).getPayload(), is(payloadEvent3));

        assertThat(paging.getPrevious(), is(nullValue()));

        assertThat(paging.getNext(), is("http://server:123/context/event-streams/" + streamId + "/" + 2 + "/" + Link.NEXT
                + "/" + 2L));

        assertThat(paging.getHead(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.HEAD + "/" + 2L));

        assertThat(paging.getLast(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.LAST + "/" + 2L));
    }

    @Test
    public void shouldReturnLinkForPreviousPageIfOnLast() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(repository, event2FeedEntryMappingStrategy,
                eventEntryFeedMaxMinProvider);

        final Stream.Builder<Event> builder = Stream.builder();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());

        builder.add(event2);

        builder.add(event1);

        when(repository.recordExists(3l, params)).thenReturn(true);

        when(repository.getFeed(0l, LAST, 2l, params)).thenReturn(builder.build());

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final Feed<EventEntry> feed = feedGenerator.feed(0l, LAST, 2l, uriInfo, params);

        assertThat(feed.getPaging().getPrevious(), is("http://server:123/context/event-streams/" + streamId + "/" + 3 + "/" + Link.PREVIOUS
                + "/" + 2L));

        assertThat(feed.getPaging().getNext(), is(nullValue()));

        assertThat(feed.getPaging().getHead(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.HEAD + "/" + 2L));

        assertThat(feed.getPaging().getLast(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.LAST + "/" + 2L));
    }

    @Test
    public void shouldReturnLinkForNextPageIfOnHead() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final Stream.Builder<Event> builder = Stream.builder();

        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());

        builder.add(event3);
        builder.add(event2);

        when(repository.recordExists(1l, params)).thenReturn(true);

        when(repository.getFeed(0l, HEAD, 2l, params)).thenReturn(builder.build());

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(repository, event2FeedEntryMappingStrategy,
                eventEntryFeedMaxMinProvider);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final Feed<EventEntry> feed = feedGenerator.feed(0, HEAD, 2l, uriInfo, params);

        assertThat(feed.getPaging().getPrevious(), is(nullValue()));

        assertThat(feed.getPaging().getNext(), is("http://server:123/context/event-streams/" + streamId + "/" + 1 + "/" + Link.NEXT
                + "/" + 2L));

        assertThat(feed.getPaging().getHead(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.HEAD + "/" + 2L));

        assertThat(feed.getPaging().getLast(), is("http://server:123/context/event-streams/" + streamId + "/" + 0 + "/" + Link.LAST + "/" + 2L));
    }
}
