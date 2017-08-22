package uk.gov.justice.services.eventsourcing.source.api.feed.event;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Entity2FeedEntryMappingStrategy;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;


public class Event2FeedEntryMappingStrategyTest {
    private static final String METADATA_JSON = "{\"field\": \"Value\"}";

    private Entity2FeedEntryMappingStrategy<Event, EventEntry> strategy = new Event2FeedEntryMappingStrategy();

    @Test
    public void shouldConvertEventToEventEntry() throws Exception {

        final UriInfo uriInfo = new ResteasyUriInfo("", "", "");

        final long sequenceId = 1L;

        final UUID eventId = randomUUID();

        final UUID streamId = randomUUID();

        final JsonObject payload1 = createObjectBuilder().add("field1", "value1").build();

        final ZonedDateTime eventCreatedAt = new UtcClock().now();

        final Event event = new Event(eventId, streamId, sequenceId, "Test Name1", METADATA_JSON, payload1.toString(), eventCreatedAt);

        final EventEntry eventEntryExpected = new EventEntry(eventId, streamId, "Test Name1", sequenceId, payload1, eventCreatedAt.toString());

        final EventEntry eventEntryActual = strategy.toFeedEntry(uriInfo).apply(event);

        assertThat(eventEntryActual.getStreamId().toString(), is(eventEntryExpected.getStreamId()));
        assertThat(eventEntryActual.getSequenceId(), is(eventEntryExpected.getSequenceId()));
        assertThat(eventEntryActual.getName(), is(eventEntryExpected.getName()));
        assertThat(eventEntryActual.getEventId(), is(eventEntryExpected.getEventId()));
        assertThat(eventEntryActual.getPayload(), is(eventEntryExpected.getPayload()));
        assertThat(eventEntryActual.getCreatedAt(), is(eventEntryExpected.getCreatedAt()));

    }
}