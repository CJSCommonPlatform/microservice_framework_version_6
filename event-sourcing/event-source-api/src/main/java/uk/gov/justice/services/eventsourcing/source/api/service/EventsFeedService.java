package uk.gov.justice.services.eventsourcing.source.api.service;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.FeedGenerator;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.Event2FeedEntryMappingStrategy;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntry;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntryFeedMaxMinProviderProvider;
import uk.gov.justice.services.jdbc.persistence.Link;
import uk.gov.justice.services.jdbc.persistence.PaginationCapableRepository;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

@ApplicationScoped
public class EventsFeedService implements FeedService<EventEntry> {

    @Inject
    private PaginationCapableRepository<Event> repository;

    private FeedGenerator<Event, EventEntry> feedGenerator;

    @PostConstruct
    public void initialise() {
        feedGenerator = new FeedGenerator<>(repository, new Event2FeedEntryMappingStrategy(), new EventEntryFeedMaxMinProviderProvider());
    }

    public Feed<EventEntry> feed(final long offset,
                                 final Link link,
                                 final long pageSize,
                                 final UriInfo uriInfo,
                                 final Map<String, Object> params) {
        return feedGenerator.feed(offset, link, pageSize, uriInfo, params);
    }
}
