package uk.gov.justice.services.eventsourcing.source.api.feed.eventstream;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Entity2FeedEntryMappingStrategy;

import java.util.function.Function;

import javax.ws.rs.core.UriInfo;

public class EventStream2FeedEntryMappingStrategy implements Entity2FeedEntryMappingStrategy<EventStream, EventStreamEntry> {

    @Override
    public Function<EventStream, EventStreamEntry> toFeedEntry(final UriInfo uriInfo) {
        return eventStream -> new EventStreamEntry(eventStream.getSequenceNumber(),
                eventStream.getStreamId(), uriInfo.getAbsolutePathBuilder().path(eventStream.getStreamId().toString()).build().toASCIIString());
    }
}
