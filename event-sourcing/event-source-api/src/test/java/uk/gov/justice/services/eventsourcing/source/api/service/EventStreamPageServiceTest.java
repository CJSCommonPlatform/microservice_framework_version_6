package uk.gov.justice.services.eventsourcing.source.api.service;

import static java.net.URI.create;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.FIRST;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.HEAD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.first;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.head;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.position;

import uk.gov.justice.services.eventsourcing.source.api.service.core.Direction;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventStreamEntry;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventStreamService;
import uk.gov.justice.services.eventsourcing.source.api.service.core.Position;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamPageServiceTest {

    private static final String BASE_URL = "http://server:123/context/";
    private static final String EVENT_STREAM_PATH = "event-streams";
    private static final int PAGE_SIZE = 2;

    @Mock
    private EventStreamService service;

    @Mock
    private UrlLinkFactory urlLinkFactory;

    @Mock
    private PositionFactory positionFactory;

    @InjectMocks
    private EventStreamPageService eventStreamPageService;

    @Test
    public void shouldReturnEventsWhenLessRecordsThanPageSizeWhenLookingForNewerEvents() throws Exception {

        final UUID streamId3 = randomUUID();

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create(BASE_URL), create(EVENT_STREAM_PATH));

        final List<EventStreamEntry> eventStreams = new ArrayList<>();

        final EventStreamEntry eventStreamEntry1 = new EventStreamEntry(streamId3.toString(), 3L);

        eventStreams.add(eventStreamEntry1);

        when(service.eventStreamExists(4L)).thenReturn(true);

        when(service.eventStreamExists(2L)).thenReturn(false);

        final Position position = position(3L);
        when(positionFactory.createPosition("3")).thenReturn(position);
        when(service.eventStreams(position, FORWARD, 2)).thenReturn(eventStreams);

        final URL streamId3SelfUrl = new URL(BASE_URL + EVENT_STREAM_PATH + streamId3 + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createEventStreamSelfUrlLink(streamId3.toString(), 2, uriInfo)).thenReturn(streamId3SelfUrl);

        final URL nextUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/4/FORWARD/2");
        when(urlLinkFactory.createEventStreamUrlLink(position(4L), FORWARD, 2, uriInfo)).thenReturn(nextUrl);

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventStreamsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventStreamsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final Page<EventStreamPageEntry> pageEvents = eventStreamPageService.pageOfEventStream("3", FORWARD, PAGE_SIZE, uriInfo);

        final List<EventStreamPageEntry> pageEventsData = pageEvents.getData();

        final PagingLinks pagingLinks = pageEvents.getPagingLinks();

        assertThat(pageEventsData, hasSize(1));
        assertThat(pageEventsData.get(0).getSelf(), is(streamId3SelfUrl.toString()));
        assertThat(pageEventsData.get(0).getSequenceNumber(), is(3L));
        assertThat(pagingLinks.getPrevious(), is(Optional.empty()));
        assertThat(pagingLinks.getNext().get(), is(nextUrl));
        assertThat(pagingLinks.getHead(), is(headURL));
        assertThat(pagingLinks.getFirst(), is(firstURL));
    }

    @Test
    public void shouldReturnEventsWhenLessRecordsThanPageSizeWhenLookingForOlderEvents() throws Exception {

        final UUID streamId3 = randomUUID();

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/"));

        final List<EventStreamEntry> eventStreams = new ArrayList<>();

        final EventStreamEntry eventsStream1 = new EventStreamEntry(streamId3.toString(), 3L);

        eventStreams.add(eventsStream1);

        when(service.eventStreamExists(4L)).thenReturn(false);

        when(service.eventStreamExists(2L)).thenReturn(true);

        final Position position = position(3L);
        when(positionFactory.createPosition("3")).thenReturn(position);
        when(service.eventStreams(position, BACKWARD, 2L)).thenReturn(eventStreams);

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventStreamsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventStreamsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final URL streamId3SelfUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/" + streamId3 + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createEventStreamSelfUrlLink(streamId3.toString(), 2, uriInfo)).thenReturn(streamId3SelfUrl);

        final URL previousUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/2/BACKWARD/2");
        when(urlLinkFactory.createEventStreamUrlLink(position(2L), BACKWARD, 2, uriInfo)).thenReturn(previousUrl);

        final Page<EventStreamPageEntry> feedActual = eventStreamPageService.pageOfEventStream("3", BACKWARD, PAGE_SIZE, uriInfo);

        final List<EventStreamPageEntry> feed = feedActual.getData();

        final PagingLinks paging = feedActual.getPagingLinks();

        assertThat(feed, hasSize(1));
        assertThat(feed.get(0).getSelf(), is(streamId3SelfUrl.toString()));
        assertThat(feed.get(0).getSequenceNumber(), is(3L));
        assertThat(paging.getNext(), is(Optional.empty()));
        assertThat(paging.getPrevious().get(), is(previousUrl));
        assertThat(paging.getHead(), is(headURL));
        assertThat(paging.getFirst(), is(firstURL));
    }

    @Test
    public void shouldReturnFeedWhenSameNumberOfRecordsAsPageSizeWhenLookingForNewerEvents() throws Exception {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();

        final List<EventStreamEntry> eventStreams = new ArrayList<>();

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/"));

        final EventStreamEntry eventsStream1 = new EventStreamEntry(streamId1.toString(), 1L);
        final EventStreamEntry eventsStream2 = new EventStreamEntry(streamId2.toString(), 2L);

        eventStreams.add(eventsStream2);
        eventStreams.add(eventsStream1);

        when(service.eventStreamExists(3L)).thenReturn(true);

        when(service.eventStreamExists(0L)).thenReturn(false);

        final Position position = position(3L);
        when(positionFactory.createPosition("3")).thenReturn(position);
        when(service.eventStreams(position, FORWARD, 2L)).thenReturn(eventStreams);

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventStreamsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventStreamsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final URL nextUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/3/FORWARD/2");
        when(urlLinkFactory.createEventStreamUrlLink(position, FORWARD, 2, uriInfo)).thenReturn(nextUrl);

        final URL streamId2SelfUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/" + streamId2 + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createEventStreamSelfUrlLink(streamId2.toString(), 2, uriInfo)).thenReturn(streamId2SelfUrl);

        final URL streamId1SelfUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/" + streamId1 + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createEventStreamSelfUrlLink(streamId1.toString(), 2, uriInfo)).thenReturn(streamId1SelfUrl);

        final Page<EventStreamPageEntry> feedActual = eventStreamPageService.pageOfEventStream("3", FORWARD, PAGE_SIZE, uriInfo);

        final List<EventStreamPageEntry> feed = feedActual.getData();

        final PagingLinks paging = feedActual.getPagingLinks();

        assertThat(feed, hasSize(2));

        assertThat(feed.get(0).getSelf(), is(streamId2SelfUrl.toString()));
        assertThat(feed.get(0).getSequenceNumber(), is(2L));
        assertThat(feed.get(1).getSelf(), is(streamId1SelfUrl.toString()));
        assertThat(feed.get(1).getSequenceNumber(), is(1L));
        assertThat(paging.getPrevious(), is(Optional.empty()));
        assertThat(paging.getNext().get(), is(nextUrl));
        assertThat(paging.getHead(), is(headURL));
        assertThat(paging.getFirst(), is(firstURL));
    }

    @Test
    public void shouldReturnFeedWhenSameNumberOfRecordsAsPageSizeWhenLookingForOlderEvents() throws Exception {
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();

        final List<EventStreamEntry> eventStreams = new ArrayList<>();

        final EventStreamEntry event4 = new EventStreamEntry(streamId4.toString(), 4L);
        final EventStreamEntry event3 = new EventStreamEntry(streamId3.toString(), 3L);

        eventStreams.add(event4);
        eventStreams.add(event3);

        when(service.eventStreamExists(5L)).thenReturn(false);
        when(service.eventStreamExists(2L)).thenReturn(true);

        final Position position = position(4L);
        when(positionFactory.createPosition("4")).thenReturn(position);
        when(service.eventStreams(position, BACKWARD, 2L)).thenReturn(eventStreams);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create(BASE_URL), create(EVENT_STREAM_PATH));

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventStreamsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventStreamsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final URL streamId3SelfUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/" + streamId4 + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createEventStreamSelfUrlLink(streamId3.toString(), 2, uriInfo)).thenReturn(streamId3SelfUrl);

        final URL streamId4SelfUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/" + streamId4 + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createEventStreamSelfUrlLink(streamId4.toString(), 2, uriInfo)).thenReturn(streamId4SelfUrl);

        final URL previousUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/2/BACKWARD/2");
        when(urlLinkFactory.createEventStreamUrlLink(position(2L), BACKWARD, 2, uriInfo)).thenReturn(previousUrl);

        final Page<EventStreamPageEntry> feedActual = eventStreamPageService.pageOfEventStream("4", BACKWARD, PAGE_SIZE, uriInfo);

        final List<EventStreamPageEntry> feed = feedActual.getData();

        final PagingLinks paging = feedActual.getPagingLinks();

        assertThat(feed, hasSize(2));

        assertThat(feed.get(0).getSelf(), is(streamId4SelfUrl.toString()));
        assertThat(feed.get(0).getSequenceNumber(), is(4L));
        assertThat(feed.get(1).getSelf(), is(streamId3SelfUrl.toString()));
        assertThat(feed.get(1).getSequenceNumber(), is(3L));
        assertThat(paging.getNext(), is(Optional.empty()));
        assertThat(paging.getPrevious().get(), is(previousUrl));
        assertThat(paging.getHead(), is(headURL));
        assertThat(paging.getFirst(), is(firstURL));
    }

    @Test
    public void shouldReturnLinkForNextPageIfOnLast() throws Exception {

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create(BASE_URL), create(EVENT_STREAM_PATH));

        final List<EventStreamEntry> eventStreams = new ArrayList<>();

        final String streamId2 = randomUUID().toString();
        final EventStreamEntry event1 = new EventStreamEntry(streamId2, 2L);

        final String streamId1 = randomUUID().toString();
        final EventStreamEntry event2 = new EventStreamEntry(streamId1, 1L);

        eventStreams.add(event2);
        eventStreams.add(event1);

        when(service.eventStreamExists(3L)).thenReturn(true);

        when(positionFactory.createPosition(FIRST)).thenReturn(first());
        when(service.eventStreams(first(), FORWARD, 2L)).thenReturn(eventStreams);

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventStreamsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventStreamsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final URL streamId2SelfUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/" + streamId2 + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createEventStreamSelfUrlLink(streamId2, 2, uriInfo)).thenReturn(streamId2SelfUrl);

        final URL streamId1SelfUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/" + streamId2 + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createEventStreamSelfUrlLink(streamId1, 2, uriInfo)).thenReturn(streamId1SelfUrl);

        final URL nextUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/3/FORWARD/2");
        when(urlLinkFactory.createEventStreamUrlLink(position(3L), FORWARD, 2, uriInfo)).thenReturn(nextUrl);

        final Page<EventStreamPageEntry> feed = eventStreamPageService.pageOfEventStream(FIRST, FORWARD, PAGE_SIZE, uriInfo);

        assertTrue(feed.getPagingLinks().getNext().get().toString().equals("http://server:123/context/event-streams/" + 3 + "/" + FORWARD + "/" + 2L));
        assertTrue(feed.getPagingLinks().getPrevious().equals(Optional.empty()));
        assertThat(feed.getPagingLinks().getHead().toString(), is("http://server:123/context/event-streams/HEAD/BACKWARD/2"));
        assertThat(feed.getPagingLinks().getFirst().toString(), is("http://server:123/context/event-streams/1/FORWARD/2"));
    }

    @Test
    public void shouldReturnLinkForPreviousPageIfOnHead() throws Exception {
        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create(BASE_URL), create(EVENT_STREAM_PATH));

        final List<EventStreamEntry> eventStreams = new ArrayList<>();

        String streamId2 = randomUUID().toString();
        final EventStreamEntry event2 = new EventStreamEntry(streamId2, 2L);
        String streamId3 = randomUUID().toString();
        final EventStreamEntry event3 = new EventStreamEntry(streamId3, 3L);

        eventStreams.add(event3);
        eventStreams.add(event2);

        when(service.eventStreamExists(1L)).thenReturn(true);

        when(positionFactory.createPosition(HEAD)).thenReturn(head());
        when(service.eventStreams(head(), BACKWARD, 2L)).thenReturn(eventStreams);

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventStreamsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventStreamsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final URL streamId3SelfUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/" + streamId3 + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createEventStreamSelfUrlLink(streamId3, 2, uriInfo)).thenReturn(streamId3SelfUrl);

        final URL streamId2SelfUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/" + streamId2 + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createEventStreamSelfUrlLink(streamId2, 2, uriInfo)).thenReturn(streamId2SelfUrl);

        final URL previousUrl = new URL(BASE_URL + EVENT_STREAM_PATH + "/1/BACKWARD/2");
        when(urlLinkFactory.createEventStreamUrlLink(position(1L), BACKWARD, 2, uriInfo)).thenReturn(previousUrl);

        final Page<EventStreamPageEntry> pageOfEventStream = eventStreamPageService.pageOfEventStream(HEAD, BACKWARD, PAGE_SIZE, uriInfo);

        assertTrue(pageOfEventStream.getPagingLinks().getNext().equals(Optional.empty()));
        PagingLinks pagingLinks = pageOfEventStream.getPagingLinks();
        assertTrue(pagingLinks.getPrevious().get().toString().equals(previousUrl.toString()));
        assertThat(pagingLinks.getHead(), is(headURL));
        assertThat(pagingLinks.getFirst(), is(firstURL));
    }

    @Test
    public void shouldReturnEmptyListWithPagingLinks() throws Exception {
        final UUID streamId = randomUUID();
        final Position sequence = position(3L);
        final Direction backward = BACKWARD;
        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"),
                create("event-streams/" + streamId));

        final List<EventStreamEntry> entries = emptyList();
        when(service.eventStreams(sequence, backward, 2)).thenReturn(entries);

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventStreamsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventStreamsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final Page<EventStreamPageEntry> eventEntryPage = eventStreamPageService.pageOfEventStream("3", backward, 2, uriInfo);
        assertThat(eventEntryPage.getData(), is(entries));
        assertThat(eventEntryPage.getPagingLinks().getFirst(), is(firstURL));
        assertThat(eventEntryPage.getPagingLinks().getHead(), is(headURL));
        assertThat(eventEntryPage.getPagingLinks().getNext(), is(empty()));
        assertThat(eventEntryPage.getPagingLinks().getPrevious(), is(empty()));

    }
}
