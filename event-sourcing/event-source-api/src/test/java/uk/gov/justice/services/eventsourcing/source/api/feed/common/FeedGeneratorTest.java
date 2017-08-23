package uk.gov.justice.services.eventsourcing.source.api.feed.common;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.Event2FeedEntryMappingStrategy;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntry;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeedGeneratorTest {

    private static final String METADATA_JSON = "{\"field\": \"Value\"}";

    @Mock
    private EventJdbcRepository repository;

    private Event2FeedEntryMappingStrategy event2FeedEntryMappingStrategy = new Event2FeedEntryMappingStrategy();

    private static ResteasyUriInfo uriInfoWithAbsoluteUri(final String absoluteUri) {
        return new ResteasyUriInfo(absoluteUri, "", "");
    }

    @Test
    public void shouldReturnFirstPageWhenMoreRecordsThanPageSize() throws Exception {

        long pageSize = 2L;

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(pageSize, repository, event2FeedEntryMappingStrategy);

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final UriInfo uriInfo = new ResteasyUriInfo("http://server:222/context/event-streams" + "/" + streamId, "", "");

        final String page = "1";

        final Stream.Builder<Event> builder = Stream.builder();

        final ZonedDateTime event1CreatedAt = new UtcClock().now();
        final ZonedDateTime event2CreatedAt = new UtcClock().now();
        final ZonedDateTime event3CreatedAt = new UtcClock().now();

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), event1CreatedAt);
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), event2CreatedAt);
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, payloadEvent3.toString(), event3CreatedAt);

        builder.add(event1);
        builder.add(event2);
        builder.add(event3);

        when(repository.getPage(0, 3, params)).thenReturn(builder.build());

        final Feed<EventEntry> feedActual = feedGenerator.feed(page, uriInfo, params);

        final List<EventEntry> streamData = feedActual.getData();

        assertThat(streamData, hasSize(2));

        assertThat(streamData.get(0).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(0).getName(), is("Test Name1"));

        assertThat(streamData.get(0).getSequenceId(), is(1L));

        assertThat(streamData.get(0).getCreatedAt(), is(event1CreatedAt.toString()));

        assertThat(streamData.get(0).getPayload(), is(payloadEvent1));

        assertThat(streamData.get(1).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(1).getSequenceId(), is(2L));

        assertThat(streamData.get(1).getPayload(), is(payloadEvent2));

        assertThat(streamData.get(1).getCreatedAt(), is(event2CreatedAt.toString()));

    }

    @Test
    public void shouldReturnFirstPageWhenLessRecordsThanPageSize() throws Exception {
        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(2L, repository, event2FeedEntryMappingStrategy);

        final UriInfo uriInfo = new ResteasyUriInfo("http://server:222/context/event-streams" + "/" + streamId, "", "");

        final String page = "1";

        final Stream.Builder<Event> builder = Stream.builder();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());

        builder.add(event1);

        when(repository.getPage(0, 3, params)).thenReturn(builder.build());

        final Feed<EventEntry> feedActual = feedGenerator.feed(page, uriInfo, params);

        final List<EventEntry> streamData = feedActual.getData();

        assertThat(streamData, hasSize(1));

        assertThat(streamData.get(0).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(0).getSequenceId(), is(1L));

        assertThat(streamData.get(0).getPayload(), is(notNullValue()));

    }

    @Test
    public void shouldReturnFeedWhenSameNumberOfRecordsAsPageSize() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final UriInfo uriInfo = new ResteasyUriInfo("http://server:222/context/event-streams" + "/" + streamId, "", "");

        final String page = "1";

        final Stream.Builder<Event> builder = Stream.builder();

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, payloadEvent1.toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, payloadEvent2.toString(), new UtcClock().now());

        builder.add(event1);
        builder.add(event2);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(2L, repository, event2FeedEntryMappingStrategy);

        when(repository.getPage(0, 3, params)).thenReturn(builder.build());

        final Feed<EventEntry> feedActual = feedGenerator.feed(page, uriInfo, params);

        final List<EventEntry> streamData = feedActual.getData();

        assertThat(streamData, hasSize(2));

        assertThat(streamData.get(0).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(0).getSequenceId(), is(1L));

        assertThat(streamData.get(0).getPayload(), is(payloadEvent1));

        assertThat(streamData.get(1).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(1).getSequenceId(), is(2L));

        assertThat(streamData.get(1).getPayload(), is(payloadEvent2));
    }

    @Test
    public void shouldReturnLinkTo2ndPage() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(2L, repository, event2FeedEntryMappingStrategy);

        final Stream.Builder<Event> builder = Stream.builder();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());

        builder.add(event1);
        builder.add(event2);
        builder.add(event3);

        when(repository.getPage(0, 3, params)).thenReturn(builder.build());

        final Feed<EventEntry> feed = feedGenerator.feed("1", uriInfoWithAbsoluteUri("http://server:123/context/event-streams/" + streamId), params);

        assertThat(feed.getPaging().getNext(), is("http://server:123/context/event-streams/" + streamId + "?page=2"));

    }

    @Test
    public void shouldNotReturnLinkToNextPageIfNoMoreRecords() {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(2L, repository, event2FeedEntryMappingStrategy);

        final Stream.Builder<Event> builder = Stream.builder();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());

        builder.add(event1);
        builder.add(event2);

        when(repository.getPage(0, 3, params)).thenReturn(builder.build());

        final Feed<EventEntry> feed = feedGenerator.feed("1", uriInfoWithAbsoluteUri("http://server:123/context/event-streams/" + streamId), params);

        assertThat(feed.getPaging().getNext(), is(nullValue()));

    }

    @Test
    public void shouldReturnLinkToPreviousPage() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(2L, repository, event2FeedEntryMappingStrategy);

        final Stream.Builder<Event> builder = Stream.builder();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());

        builder.add(event1);
        builder.add(event2);
        builder.add(event3);

        when(repository.getPage(2, 3, params)).thenReturn(builder.build());

        final Feed<EventEntry> feed = feedGenerator.feed("2", uriInfoWithAbsoluteUri("http://server:234/context/event-streams/" + streamId), params);

        assertThat(feed.getPaging().getPrevious(), is("http://server:234/context/event-streams/" + streamId + "?page=1"));

    }

    @Test
    public void shouldNotReturnLinkToPreviousPageIfOnFirstPage() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final Stream.Builder<Event> builder = Stream.builder();

        final Event event1 = new Event(randomUUID(), streamId, 1L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field1", "value1").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 2L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field2", "value2").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 3L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field3", "value3").build().toString(), new UtcClock().now());

        builder.add(event1);
        builder.add(event2);
        builder.add(event3);

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(2L, repository, event2FeedEntryMappingStrategy);

        when(repository.getPage(0, 3, params)).thenReturn(builder.build());

        final Feed<EventEntry> feed = feedGenerator.feed("1", uriInfoWithAbsoluteUri("http://server:234/context/streams"), params);

        assertThat(feed.getPaging().getPrevious(), is(nullValue()));

    }

    @Test
    public void shouldSendSameArgumentsToRepositoryQueryingForPage3() throws Exception {
        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final UriInfo uriInfo = new ResteasyUriInfo("http://server:222/context/event-streams" + "/" + streamId, "", "");

        final String page = "3";

        final FeedGenerator<Event, EventEntry> feedGenerator = new FeedGenerator<>(10L, repository, event2FeedEntryMappingStrategy);

        final Stream.Builder<Event> builder = Stream.builder();

        final Event event1 = new Event(randomUUID(), streamId, 21L, "Test Name1", METADATA_JSON, createObjectBuilder().add("field21", "value21").build().toString(), new UtcClock().now());
        final Event event2 = new Event(randomUUID(), streamId, 22L, "Test Name2", METADATA_JSON, createObjectBuilder().add("field22", "value22").build().toString(), new UtcClock().now());
        final Event event3 = new Event(randomUUID(), streamId, 23L, "Test Name3", METADATA_JSON, createObjectBuilder().add("field23", "value23").build().toString(), new UtcClock().now());

        builder.add(event1);
        builder.add(event2);
        builder.add(event3);


        when(repository.getPage(20, 11, params)).thenReturn(builder.build());

        final Feed<EventEntry> feedActual = feedGenerator.feed(page, uriInfo, params);

        final List<EventEntry> streamData = feedActual.getData();
        assertThat(streamData, hasSize(3));

        final ArgumentCaptor<Long> pageCaptor = ArgumentCaptor.forClass(Long.class);
        final ArgumentCaptor<Long> offsetCaptor = ArgumentCaptor.forClass(Long.class);
        final ArgumentCaptor<HashMap> paramsCaptor = ArgumentCaptor.forClass(HashMap.class);

        verify(repository).getPage(offsetCaptor.capture(), pageCaptor.capture(), paramsCaptor.capture());

        assertThat(offsetCaptor.getValue(), is(20L));

        assertThat(pageCaptor.getValue(), is(11l));

        assertThat(paramsCaptor.getValue().get("STREAM_ID"), is(streamId));
    }

}
