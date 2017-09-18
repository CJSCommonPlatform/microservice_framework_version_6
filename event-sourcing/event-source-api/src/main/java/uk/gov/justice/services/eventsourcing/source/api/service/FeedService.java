package uk.gov.justice.services.eventsourcing.source.api.service;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.jdbc.persistence.Link;

import java.util.Map;

import javax.ws.rs.core.UriInfo;

public interface FeedService<T> {
    Feed<T> feed(final long offset, final Link link, final long pagesize, final UriInfo uriInfo, final Map<String, Object> params);
}
