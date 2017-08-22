package uk.gov.justice.services.eventsourcing.source.api.feed.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Entity2FeedEntryMappingStrategy;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;


public class Event2FeedEntryMappingStrategyTest {

    private static final String NAME = "Test Name";
    private static final String PAYLOAD_JSON = "{\"field\": \"Value\"}";
    private static final String METADATA_JSON = "{\"field\": \"Value\"}";
    private final static ZonedDateTime TIMESTAMP = new UtcClock().now();

    private Entity2FeedEntryMappingStrategy<Event, EventEntry> strategy = new Event2FeedEntryMappingStrategy();

    @Test
    public void shouldConvertEventToEventEntry() throws Exception {

        final UriInfo uriInfo = new ResteasyUriInfo("", "", "");

        final long sequenceId = 1L;

        final UUID eventId = randomUUID();

        final UUID streamId = randomUUID();

        final EventEntry eventEntryExpected = eventEntryOf(eventId, sequenceId, streamId);

        final EventEntry eventEntryActual = strategy.toFeedEntry(uriInfo).apply(eventOf(eventId, sequenceId, streamId));

        assertThat(eventEntryActual.getStreamId().toString(), is(eventEntryExpected.getStreamId()));
        assertThat(eventEntryActual.getSequenceId(), is(eventEntryExpected.getSequenceId()));
        assertThat(eventEntryActual.getName(), is(eventEntryExpected.getName()));
        assertThat(eventEntryActual.getEventId(), is(eventEntryExpected.getEventId()));
        assertThat(eventEntryActual.getPayload().getPayloadContent(), is(eventEntryExpected.getPayload().getPayloadContent()));
        assertThat(eventEntryActual.getPayload().getStreamId(), is(eventEntryExpected.getStreamId()));
        assertThat(eventEntryActual.getCreatedAt(), is(eventEntryExpected.getCreatedAt()));

    }

    private EventEntry eventEntryOf(final UUID eventId, final long sequenceId, final UUID streamId) {
        return new EventEntry(eventId, streamId, NAME, sequenceId, TIMESTAMP, new EventPayload(streamId.toString(), PAYLOAD_JSON));
    }

    private Event eventOf(final UUID eventId, final long sequenceId, final UUID streamId) {
        return new Event(eventId, streamId, sequenceId, NAME, METADATA_JSON, PAYLOAD_JSON, TIMESTAMP);
    }
}