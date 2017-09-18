package uk.gov.justice.services.eventsourcing.source.api.feed.event;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.FeedMaxMinProvider;

import java.util.List;

public class EventEntryFeedMaxMinProviderProvider implements FeedMaxMinProvider<EventEntry> {

    @Override
    public long min(List<EventEntry> eventStreams) {
        return eventStreams.stream().mapToLong(e -> e.getSequenceId()).min().orElse(ZERO);
    }

    @Override
    public long max(List<EventEntry> eventStreams) {
        return eventStreams.stream().mapToLong(e -> e.getSequenceId()).max().orElse(ZERO);
    }
}
