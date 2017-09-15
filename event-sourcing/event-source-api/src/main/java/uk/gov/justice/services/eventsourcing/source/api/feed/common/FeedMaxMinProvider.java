package uk.gov.justice.services.eventsourcing.source.api.feed.common;

import java.util.List;

/**
 * Max Min provider
 *
 * @param <FE> - feed entry type
 */

public interface FeedMaxMinProvider<FE> {

    long min(final List<FE> eventStreams);

    long max(final List<FE> eventStreams);

}
