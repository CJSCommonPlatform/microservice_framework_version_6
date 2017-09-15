package uk.gov.justice.services.eventsourcing.source.api.feed.common;

import java.util.List;

/**
 * Max Min provider
 *
 * @param <E> - feed entry type
 */

public interface FeedMaxMinProvider<E> {

    static final long ZERO = 0L;

    long min(final List<E> eventStreams);

    long max(final List<E> eventStreams);

}
