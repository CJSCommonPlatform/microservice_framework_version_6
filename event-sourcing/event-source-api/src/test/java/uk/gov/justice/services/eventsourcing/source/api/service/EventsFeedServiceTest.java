package uk.gov.justice.services.eventsourcing.source.api.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.FeedGenerator;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Paging;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntry;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventPayload;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventsFeedServiceTest {

    private static final String NAME = "Test Name";
    private static final String PAYLOAD_JSON = "{\"field\": \"Value\"}";
    private static final String METADATA_JSON = "{\"field\": \"Value\"}";
    private final static ZonedDateTime TIMESTAMP = new UtcClock().now();

    @Mock
    private EventJdbcRepository repository;

    @Mock
    private FeedGenerator<Event, EventEntry> feedGenerator;

    @InjectMocks
    private EventsFeedService service;

    @Test
    public void shouldReturnFirstPageWhenMoreRecordsThanPageSize() throws Exception {

        final UUID streamId = randomUUID();

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        final UriInfo uriInfo = new ResteasyUriInfo("http://server:222/context/event-streams" + "/" + streamId, "", "");

        final String page = "1";

        final Stream.Builder<EventEntry> eventEntryBuilder = Stream.builder();
        final Stream.Builder<Event> eventBuilder = Stream.builder();

        for (long sequence = 1; sequence < 4l; sequence++) {
            eventEntryBuilder.add(eventEntryOf(sequence, streamId));
            eventBuilder.add(eventOf(sequence, streamId));
        }
        final long pageSize = 2L;
        initialiseWithPageSize(service, pageSize);

        final Paging paging = new Paging(null, null);

        final long offset = 0L;

        when(repository.getPage(offset, pageSize + 1, params)).thenReturn(eventBuilder.build());

        when(feedGenerator.feed(page, uriInfo, params)).thenReturn(new Feed(eventEntryBuilder.build().collect(Collectors.toList()), paging));

        final Feed<EventEntry> feedActual = service.feed(page, uriInfo, params);

        final List<EventEntry> streamData = feedActual.getData();

        final Paging pagingActual = feedActual.getPaging();

        assertThat(streamData, hasSize(2));

        assertThat(streamData.get(0).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(0).getName(), is(NAME));

        assertThat(streamData.get(0).getSequenceId(), is(1L));

        assertThat(streamData.get(0).getCreatedAt(), is(TIMESTAMP));

        assertThat(streamData.get(0).getPayload(), is(notNullValue()));

        assertThat(streamData.get(0).getPayload().getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(0).getPayload().getPayloadContent(), is(PAYLOAD_JSON));

        assertThat(streamData.get(1).getStreamId(), is(streamId.toString()));

        assertThat(streamData.get(1).getSequenceId(), is(2L));

        assertThat(streamData.get(1).getPayload(), is(notNullValue()));

        assertThat(streamData.get(1).getPayload().getStreamId(), is(streamId.toString()));

        assertThat(pagingActual.getPrevious(), is(nullValue()));

        assertThat(pagingActual.getNext(), is("http://server:222/context/event-streams/" + streamId + "?page=2"));
    }

    private void initialiseWithPageSize(final EventsFeedService service, final long pageSize) {
        service.pageSize = pageSize;
        service.initialise();
    }

    private EventEntry eventEntryOf(final long sequenceId, final UUID streamId) {
        return new EventEntry(randomUUID(), streamId, NAME, sequenceId, TIMESTAMP, new EventPayload(streamId.toString(), PAYLOAD_JSON));
    }

    private Event eventOf(final long sequenceId, final UUID streamId) {
        return new Event(randomUUID(), streamId, sequenceId, NAME, METADATA_JSON, PAYLOAD_JSON, TIMESTAMP);
    }

}