package uk.gov.justice.services.eventsourcing.source.api.feed.event;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.FeedMaxMinProvider;

import java.util.Comparator;
import java.util.List;

public class EventEntryFeedMaxMinProviderProvider implements FeedMaxMinProvider<EventEntry> {

    final Comparator<EventEntry> sequenceComparator = (p1, p2) -> Long.compare(p1.getSequenceId(), p2.getSequenceId());

    @Override
    public long min(List<EventEntry> eventStreams) {
        if (eventStreams.isEmpty()) {
            return 0;
        }
        return eventStreams.stream().min(sequenceComparator).get().getSequenceId();
    }

    @Override
    public long max(List<EventEntry> eventStreams) {
        if (eventStreams.isEmpty()) {
            return 0;
        }
        return eventStreams.stream().max(sequenceComparator).get().getSequenceId();
    }
}
