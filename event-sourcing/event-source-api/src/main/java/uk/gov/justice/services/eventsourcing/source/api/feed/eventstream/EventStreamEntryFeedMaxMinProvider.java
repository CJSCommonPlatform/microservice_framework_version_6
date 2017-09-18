package uk.gov.justice.services.eventsourcing.source.api.feed.eventstream;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.FeedMaxMinProvider;

import java.util.List;

public class EventStreamEntryFeedMaxMinProvider implements FeedMaxMinProvider<EventStreamEntry> {

    @Override
    public long min(List<EventStreamEntry> eventStreams) {
        return eventStreams.stream().mapToLong(e -> e.getSequenceId()).min().orElse(ZERO);
    }

    @Override
    public long max(List<EventStreamEntry> eventStreams) {
        return eventStreams.stream().mapToLong(e -> e.getSequenceId()).max().orElse(ZERO);
    }
}
