package uk.gov.justice.services.eventsourcing.source.api.feed.common;

import java.util.function.Function;

import javax.ws.rs.core.UriInfo;

/**
 * Strategy for mapping entities to feed entries
 *
 * @param <E>  - entity type
 * @param <FE> - feed entry type
 */
public interface Entity2FeedEntryMappingStrategy<E, FE> {
    Function<E, FE> toFeedEntry(final UriInfo uriInfo);
}
