package uk.gov.justice.services.eventsourcing.source.api.service;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.FeedGenerator;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.FeedMaxMinProvider;
import uk.gov.justice.services.eventsourcing.source.api.feed.eventstream.EventStream2FeedEntryMappingStrategy;
import uk.gov.justice.services.eventsourcing.source.api.feed.eventstream.EventStreamEntry;
import uk.gov.justice.services.jdbc.persistence.Link;
import uk.gov.justice.services.jdbc.persistence.PaginationCapableRepository;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

@ApplicationScoped
public class EventStreamsFeedService implements FeedService<EventStreamEntry> {

    @Inject
    private PaginationCapableRepository<EventStream> repository;

    private FeedGenerator<EventStream, EventStreamEntry> feedGenerator;

    private FeedMaxMinProvider<EventStreamEntry> feedMaxMinProvider;

    @PostConstruct
    public void initialise() {
        feedGenerator = new FeedGenerator<>(repository, new EventStream2FeedEntryMappingStrategy(), null);
    }

    public Feed<EventStreamEntry> feed(final long offset,
                                       final Link link,
                                       final long pageSize,
                                       final UriInfo uriInfo,
                                       final Map<String, Object> params) {
        return feedGenerator.feed(offset, link, pageSize, uriInfo, params);
    }


}
