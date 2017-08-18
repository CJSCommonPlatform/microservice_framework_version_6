package uk.gov.justice.services.eventsourcing.source.api.feed.common;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.jdbc.persistence.PaginationCapableRepository;

import java.util.List;

import javax.ws.rs.core.UriInfo;

/**
 * Generator of paginated feeds
 *
 * @param <E> - entity generic type
 * @param <FE> - feed entry generic type
 */
public class FeedGenerator<E, FE> {

    private final long pageSize;

    private final PaginationCapableRepository<E> repository;

    private final Entity2FeedEntryMappingStrategy<E, FE> mappingStrategy;

    public FeedGenerator(final long pageSize, final PaginationCapableRepository<E> repository, final Entity2FeedEntryMappingStrategy<E, FE> mappingStrategy) {
        this.pageSize = pageSize;
        this.repository = repository;
        this.mappingStrategy = mappingStrategy;
    }

    public Feed<FE> feed(final String page, final UriInfo uriInfo) {
        final long pageNumber = Long.valueOf(page);
        final List<E> entities = entitiesPage(pageSize, pageNumber);

        return feed(uriInfo, pageNumber, entities);
    }

    private Feed<FE> feed(final UriInfo uriInfo, final long pageNumber, final List<E> entities) {
        final int entityCount = entities.size();
        return new Feed<>(
                feedEntries(entities, uriInfo).subList(0, entityCount > pageSize ? entityCount - 1 : entityCount),
                new Paging(previousPageHrefFrom(uriInfo, pageNumber), nextPageHrefFrom(uriInfo, entities, pageNumber, pageSize)));
    }

    private List<E> entitiesPage(final long pageSize, final long pageNumber) {
        long offset = (pageNumber - 1) * pageSize;
        return repository.getPage(offset, pageSize + 1).collect(toList());
    }

    private String previousPageHrefFrom(final UriInfo uriInfo, final long pageNumber) {
        return pageNumber > 1 ? pageHref(uriInfo, pageNumber - 1) : null;
    }


    private String nextPageHrefFrom(final UriInfo uriInfo, final List<E> eventStreams, final long pageNumber, final long pageSize) {
        return pageSize == eventStreams.size() - 1 ? pageHref(uriInfo, pageNumber + 1) : null;
    }

    private String pageHref(final UriInfo uriInfo, final long page) {
        return uriInfo.getAbsolutePathBuilder().queryParam("page", page).build().toASCIIString();
    }

    private List<FE> feedEntries(final List<E> eventStreams, final UriInfo uriInfo) {
        return eventStreams.stream()
                .map(mappingStrategy.toFeedEntry(uriInfo))
                .collect(toList());
    }


}
