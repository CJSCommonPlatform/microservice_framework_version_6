package uk.gov.justice.services.eventsourcing.source.api.feed.event;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Entity2FeedEntryMappingStrategy;

import java.time.ZonedDateTime;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.UriInfo;

public class Event2FeedEntryMappingStrategy implements Entity2FeedEntryMappingStrategy<Event, EventEntry> {

    @Override
    public Function<Event, EventEntry> toFeedEntry(final UriInfo uriInfo) {
        return event -> new EventEntry(
                event.getId(),
                event.getStreamId(),
                event.getName(),
                event.getSequenceId(),
                convert(event.getPayload()),
                convertToTimestamp(event.getCreatedAt())
        );
    }

    private String convertToTimestamp(final ZonedDateTime createdAt) {
        return ZonedDateTimes.toString(createdAt);
    }

    private JsonObject convert(final String payload) {
        return new StringToJsonObjectConverter().convert(payload);
    }
}
