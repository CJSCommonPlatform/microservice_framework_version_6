package uk.gov.justice.services.eventsourcing.source.api.feed.common;

import static java.net.URI.create;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition.FIRST;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition.HEAD;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntry;
import uk.gov.justice.services.eventsourcing.source.api.service.EventsPageService;
import uk.gov.justice.services.eventsourcing.source.api.service.EventsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventsPageServiceTest {

    @Mock
    private EventsService service;

    @InjectMocks
    EventsPageService eventsPageService;

    @Test
    public void shouldReturnEventsWhenLessRecordsThanPageSizeWhenLookingForNewerEvents() throws Exception {

        final UUID streamId = randomUUID();

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final List<EventEntry> events = new ArrayList<>();

        final EventEntry event1 = new EventEntry(randomUUID(), streamId, 3L, "Test Name3", createObjectBuilder().add("field3", "value3").build(), new UtcClock().now().toString());

        events.add(event1);

        when(service.recordExists(streamId, 4L)).thenReturn(true);

        when(service.recordExists(streamId, 2L)).thenReturn(false);

        when(service.events(eq(streamId), argThat(new PositionArgumentMatcher(3L)), eq(BACKWARD), eq(2L))).thenReturn(events);

        final Page<EventEntry> pageEvents = eventsPageService.pageEvents(streamId, "3", BACKWARD, 2L, uriInfo);

        final List<EventEntry> pageEventsData = pageEvents.getData();

        final PagingLinks pagingLinks = pageEvents.getPagingLinks();

        assertThat(pageEventsData, hasSize(1));

        assertThat(pageEventsData.get(0).getStreamId(), is(streamId.toString()));

        assertThat(pageEventsData.get(0).getSequenceId(), is(3L));

        assertThat(pageEventsData.get(0).getPayload(), is(notNullValue()));

        assertThat(pagingLinks.getNext(), is(Optional.empty()));

        assertTrue(pagingLinks.getPrevious().get().toString().equals("http://server:123/context/event-streams/" + streamId + "/" + 4 + "/" + FORWARD + "/" + 2L));

        assertThat(pagingLinks.getHead().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + HEAD + "/" + BACKWARD + "/" + 2L));

        assertThat(pagingLinks.getFirst().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + FIRST.getPosition() + "/" + FORWARD + "/" + 2L));
    }

    @Test
    public void shouldReturnEventsWhenLessRecordsThanPageSizeWhenLookingForOlderEvents() throws Exception {

        final UUID streamId = randomUUID();

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final List<EventEntry> events = new ArrayList<>();

        final EventEntry event1 = new EventEntry(randomUUID(), streamId, 3L, "Test Name3", createObjectBuilder().add("field3", "value3").build(), new UtcClock().now().toString());

        events.add(event1);

        when(service.recordExists(streamId, 4L)).thenReturn(false);

        when(service.recordExists(streamId, 2L)).thenReturn(true);

        when(service.events(eq(streamId), argThat(new PositionArgumentMatcher(3L)), eq(BACKWARD), eq(2L))).thenReturn(events);

        final Page<EventEntry> feedActual = eventsPageService.pageEvents(streamId, "3", BACKWARD, 2L, uriInfo);

        final List<EventEntry> feed = feedActual.getData();

        final PagingLinks paging = feedActual.getPagingLinks();

        assertThat(feed, hasSize(1));

        assertThat(feed.get(0).getStreamId(), is(streamId.toString()));

        assertThat(feed.get(0).getSequenceId(), is(3L));

        assertThat(feed.get(0).getPayload(), is(notNullValue()));

        assertThat(paging.getPrevious(), is(Optional.empty()));

        assertTrue(paging.getNext().get().toString().equals("http://server:123/context/event-streams/" + streamId + "/" + 2 + "/" + BACKWARD + "/" + 2L));

        assertThat(paging.getHead().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + HEAD + "/" + BACKWARD + "/" + 2L));

        assertThat(paging.getFirst().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + FIRST.getPosition() + "/" + FORWARD + "/" + 2L));
    }

    @Test
    public void shouldReturnFeedWhenSameNumberOfRecordsAsPageSizeWhenLookingForNewerEvents() throws Exception {
        final UUID streamId = randomUUID();

        final List<EventEntry> events = new ArrayList<>();

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();

        final EventEntry event1 = new EventEntry(randomUUID(), streamId, 1L, "Test Name1", payloadEvent1, new UtcClock().now().toString());
        final EventEntry event2 = new EventEntry(randomUUID(), streamId, 2L, "Test Name2", payloadEvent2, new UtcClock().now().toString());
        events.add(event2);
        events.add(event1);

        when(service.recordExists(streamId, 3L)).thenReturn(true);

        when(service.recordExists(streamId, 0L)).thenReturn(false);

        when(service.events(eq(streamId), argThat(new PositionArgumentMatcher(3L)), eq(FORWARD), eq(2L))).thenReturn(events);

        final Page<EventEntry> feedActual = eventsPageService.pageEvents(streamId, "3", FORWARD, 2L, uriInfo);

        final List<EventEntry> feed = feedActual.getData();

        final PagingLinks paging = feedActual.getPagingLinks();

        assertThat(feed, hasSize(2));

        assertThat(feed.get(0).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(0).getSequenceId(), is(2L));
        assertThat(feed.get(0).getPayload(), is(payloadEvent2));
        assertThat(feed.get(1).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(1).getSequenceId(), is(1L));
        assertThat(feed.get(1).getPayload(), is(payloadEvent1));

        assertTrue(paging.getNext().equals(Optional.empty()));

        assertTrue(paging.getPrevious().get().toString().equals("http://server:123/context/event-streams/" + streamId + "/" + 3 + "/" + FORWARD + "/" + 2L));

        assertThat(paging.getHead().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + HEAD + "/" + BACKWARD + "/" + 2L));

        assertThat(paging.getFirst().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + FIRST.getPosition() + "/" + FORWARD + "/" + 2L));
    }


    @Test
    public void shouldReturnFeedWhenSameNumberOfRecordsAsPageSizeWhenLookingForOlderEvents() throws Exception {
        final UUID streamId = randomUUID();

        final List<EventEntry> events = new ArrayList<>();

        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();

        final EventEntry event4 = new EventEntry(randomUUID(), streamId, 4L, "Test Name4", payloadEvent4, new UtcClock().now().toString());
        final EventEntry event3 = new EventEntry(randomUUID(), streamId, 3L, "Test Name3", payloadEvent3, new UtcClock().now().toString());

        events.add(event4);
        events.add(event3);

        when(service.recordExists(streamId, 5L)).thenReturn(false);

        when(service.recordExists(streamId, 2L)).thenReturn(true);

        when(service.events(eq(streamId), argThat(new PositionArgumentMatcher(4L)), eq(BACKWARD), eq(2L))).thenReturn(events);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final Page<EventEntry> feedActual = eventsPageService.pageEvents(streamId, "4", BACKWARD, 2L, uriInfo);

        final List<EventEntry> feed = feedActual.getData();

        final PagingLinks paging = feedActual.getPagingLinks();

        assertThat(feed, hasSize(2));
        assertThat(feed.get(0).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(0).getSequenceId(), is(4L));
        assertThat(feed.get(0).getPayload(), is(payloadEvent4));
        assertThat(feed.get(1).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(1).getSequenceId(), is(3L));
        assertThat(feed.get(1).getPayload(), is(payloadEvent3));

        assertThat(paging.getPrevious(), is(Optional.empty()));

        assertTrue(paging.getNext().isPresent() && paging.getNext().get().toString().equals("http://server:123/context/event-streams/" + streamId + "/" + 2 + "/" + BACKWARD + "/" + 2L));

        assertThat(paging.getHead().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + HEAD + "/" + BACKWARD + "/" + 2L));

        assertThat(paging.getFirst().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + FIRST.getPosition() + "/" + FORWARD + "/" + 2L));
    }

    @Test
    public void shouldReturnLinkForPreviousPageIfOnLast() throws Exception {

        final UUID streamId = randomUUID();

        final List<EventEntry> events = new ArrayList<>();

        final EventEntry event1 = new EventEntry(randomUUID(), streamId, 1L, "Test Name1", createObjectBuilder().add("field1", "value1").build(), new UtcClock().now().toString());
        final EventEntry event2 = new EventEntry(randomUUID(), streamId, 2L, "Test Name2", createObjectBuilder().add("field2", "value2").build(), new UtcClock().now().toString());

        events.add(event2);

        events.add(event1);

        when(service.recordExists(streamId, 3l)).thenReturn(true);

        when(service.events(eq(streamId), argThat(new PositionArgumentMatcher(HEAD.getPosition())), eq(FORWARD), eq(2l))).thenReturn(events);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final Page<EventEntry> feed = eventsPageService.pageEvents(streamId, FIRST.getPosition(), FORWARD, 2l, uriInfo);

        assertTrue(feed.getPagingLinks().getPrevious().get().toString().equals("http://server:123/context/event-streams/" + streamId + "/" + 3 + "/" + FORWARD + "/" + 2L));

        assertTrue(feed.getPagingLinks().getNext().equals(Optional.empty()));

        assertThat(feed.getPagingLinks().getHead().toString().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + HEAD + "/" + BACKWARD + "/" + 2L));

        assertThat(feed.getPagingLinks().getFirst().toString().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + FIRST.getPosition() + "/" + FORWARD + "/" + 2L));
    }

    @Test
    public void shouldReturnLinkForNextPageIfOnHead() throws Exception {

        final UUID streamId = randomUUID();

        final List<EventEntry> events = new ArrayList<>();

        final EventEntry event2 = new EventEntry(randomUUID(), streamId, 2L, "Test Name2", createObjectBuilder().add("field2", "value2").build(), new UtcClock().now().toString());
        final EventEntry event3 = new EventEntry(randomUUID(), streamId, 3L, "Test Name3", createObjectBuilder().add("field3", "value3").build(), new UtcClock().now().toString());

        events.add(event3);
        events.add(event2);

        when(service.recordExists(streamId, 1l)).thenReturn(true);

        when(service.events(eq(streamId), argThat(new PositionArgumentMatcher(HEAD.getPosition())), eq(BACKWARD), eq(2l))).thenReturn(events);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final Page<EventEntry> feed = eventsPageService.pageEvents(streamId, HEAD.getPosition(), BACKWARD, 2l, uriInfo);

        assertTrue(feed.getPagingLinks().getPrevious().equals(Optional.empty()));
        assertTrue(feed.getPagingLinks().getNext().get().toString().equals("http://server:123/context/event-streams/" + streamId + "/" + 1 + "/" + FORWARD + "/" + 2L));
        assertThat(feed.getPagingLinks().getHead().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + HEAD + "/" + BACKWARD + "/" + 2L));
        assertThat(feed.getPagingLinks().getFirst().toString(), is("http://server:123/context/event-streams/" + streamId + "/" + FIRST.getPosition() + "/" + FORWARD + "/" + 2L));
    }

}
