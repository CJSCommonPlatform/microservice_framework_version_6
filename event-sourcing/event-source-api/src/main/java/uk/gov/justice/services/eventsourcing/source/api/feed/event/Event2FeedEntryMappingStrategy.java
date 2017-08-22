package uk.gov.justice.services.eventsourcing.source.api.feed.event;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Entity2FeedEntryMappingStrategy;

import java.util.function.Function;

import javax.ws.rs.core.UriInfo;

public class Event2FeedEntryMappingStrategy implements Entity2FeedEntryMappingStrategy<Event, EventEntry> {

    @Override
    public Function<Event, EventEntry> toFeedEntry(final UriInfo uriInfo) {
        return event -> new EventEntry(
                event.getId(),
                event.getStreamId(),
                event.getName(),
                event.getSequenceId(),
                event.getCreatedAt(),
                new EventPayload(event.getStreamId().toString(), event.getPayload()));
    }
}
