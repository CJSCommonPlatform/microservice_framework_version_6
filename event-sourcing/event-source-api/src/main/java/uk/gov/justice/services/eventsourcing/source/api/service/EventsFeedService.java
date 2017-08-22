package uk.gov.justice.services.eventsourcing.source.api.service;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.FeedGenerator;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.Event2FeedEntryMappingStrategy;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntry;
import uk.gov.justice.services.jdbc.persistence.PaginationCapableRepository;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

@ApplicationScoped
public class EventsFeedService implements FeedService<EventEntry> {

    @Value(key = "eventsFeedSize", defaultValue = "25")
    protected long pageSize;

    @Inject
    private PaginationCapableRepository<Event> repository;

    private FeedGenerator<Event, EventEntry> feedGenerator;

    @PostConstruct
    public void initialise() {
        feedGenerator = new FeedGenerator<>(pageSize, repository, new Event2FeedEntryMappingStrategy());
    }

    public Feed<EventEntry> feed(final String page, final UriInfo uriInfo, final Map<String, Object> params) {
        return feedGenerator.feed(page, uriInfo, params);
    }


}
