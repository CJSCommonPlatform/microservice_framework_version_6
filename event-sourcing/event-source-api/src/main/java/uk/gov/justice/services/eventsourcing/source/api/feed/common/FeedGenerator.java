package uk.gov.justice.services.eventsourcing.source.api.feed.common;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.jdbc.persistence.Link.HEAD;
import static uk.gov.justice.services.jdbc.persistence.Link.LAST;
import static uk.gov.justice.services.jdbc.persistence.Link.NEXT;
import static uk.gov.justice.services.jdbc.persistence.Link.PREVIOUS;

import uk.gov.justice.services.jdbc.persistence.Link;
import uk.gov.justice.services.jdbc.persistence.PaginationCapableRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

/**
 * Generator of paginated feeds
 *
 * @param <E>  - entity generic type
 * @param <FE> - feed entry generic type
 */
public class FeedGenerator<E, FE> {

    private final PaginationCapableRepository<E> repository;

    private final Entity2FeedEntryMappingStrategy<E, FE> mappingStrategy;

    private final FeedMaxMinProvider<FE> feedMaxMinProvider;

    public FeedGenerator(final PaginationCapableRepository<E> repository,
                         final Entity2FeedEntryMappingStrategy<E, FE> mappingStrategy,
                         final FeedMaxMinProvider<FE> feedMaxMinProvider) {
        this.repository = repository;
        this.mappingStrategy = mappingStrategy;
        this.feedMaxMinProvider = feedMaxMinProvider;
    }

    public Feed<FE> feed(final long offset,
                         final Link link,
                         final long pageSize,
                         final UriInfo uriInfo,
                         final Map<String, Object> params) {

        final List<E> entities = entitiesPage(offset, link, pageSize, params);

        final List<FE> feedEntries = feedEntries(entities, uriInfo);

        return feedWithLinks(link, pageSize, uriInfo, params, feedEntries);
    }

    private Feed<FE> feedWithLinks(final Link link, final long pageSize, final UriInfo uriInfo,
                                   final Map<String, Object> params, final List<FE> feedEntries) {

        if (feedEntries.isEmpty()) {
            return new Feed<>(new ArrayList<>(),
                    new Paging(null, null, pageHref(0, HEAD, pageSize, uriInfo),
                            pageHref(0, LAST, pageSize, uriInfo)));
        }

        final long maxSequenceId = feedMaxMinProvider.max(feedEntries);
        final long minSequenceId = feedMaxMinProvider.min(feedEntries);

        final boolean newEventsAvailable = link != HEAD && recordExists(maxSequenceId + 1, PREVIOUS, pageSize,
                params);

        final boolean olderEventsAvailable = link != LAST && recordExists(minSequenceId - 1, NEXT, pageSize, params);

        return new Feed<>(feedEntries, new Paging(
                newEventsAvailable ? pageHref(maxSequenceId + 1, PREVIOUS, pageSize, uriInfo) : null,
                olderEventsAvailable ? pageHref(minSequenceId - 1, NEXT, pageSize, uriInfo) : null,
                pageHref(0, HEAD, pageSize, uriInfo),
                pageHref(0, LAST, pageSize, uriInfo)));
    }

    private List<E> entitiesPage(final long offset, final Link link, final long pageSize,
                                 final Map<String, Object> params) {
        return repository.getFeed(offset, link, pageSize, params).collect(toList());
    }

    private boolean recordExists(final long offset, final Link link, final long pageSize,
                                 final Map<String, Object> params) {
        return repository.recordExists(offset, link, pageSize, params);
    }


    private String pageHref(final long offset,
                            final Link link,
                            final long pageSize,
                            final UriInfo uriInfo) {

        return uriInfo.getBaseUriBuilder()
                .path(uriInfo.getPathSegments().get(0).getPath())
                .path(uriInfo.getPathSegments().get(1).getPath())
                .path(String.valueOf(offset))
                .path(link.toString())
                .path(String.valueOf(pageSize)).build()
                .toString();

    }

    private List<FE> feedEntries(final List<E> eventStreams, final UriInfo uriInfo) {
        return eventStreams.stream()
                .map(mappingStrategy.toFeedEntry(uriInfo))
                .collect(toList());
    }
}
