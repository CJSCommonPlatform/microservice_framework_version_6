package uk.gov.justice.services.eventsourcing.source.api.service;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;

import java.util.Map;

import javax.ws.rs.core.UriInfo;

public interface FeedService<T> {
    Feed<T> feed(final String page, final UriInfo uriInfo, final Map<String, Object> params);

}
