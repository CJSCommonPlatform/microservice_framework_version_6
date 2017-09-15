package uk.gov.justice.services.eventsourcing.source.api.resource;


import static uk.gov.justice.services.jdbc.persistence.Link.valueOf;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventsFeedService;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

@Path("event-streams/{streamId}/{offset}/{link}/{pageSize}")
public class EventFeedResource {

    @Context
    HttpHeaders headers;

    @Inject
    private EventsFeedService service;

    @Inject
    private AccessController accessController;

    @GET
    @Produces("application/vnd.event-source.events+json")
    public Feed events(
            @PathParam("streamId") final String streamId,
            @PathParam("offset") long offset,
            @PathParam("link") String link,
            @PathParam("pageSize") long pageSize,
            @Context UriInfo uriInfo) {

        accessController.checkAccessControl(headers);

        final Map<String, Object> params = new HashMap();
        params.put("STREAM_ID", streamId);

        return service.feed(offset, valueOf(link), pageSize, uriInfo, params);

    }
}