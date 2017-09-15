package uk.gov.justice.services.eventsourcing.source.api.resource;

import static uk.gov.justice.services.jdbc.persistence.Link.valueOf;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventStreamsFeedService;

import java.util.HashMap;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

@Path("event-streams/{offset}/{link}/{pageSize}")
public class EventStreamsFeedResource {

    @Context
    HttpHeaders headers;

    @Inject
    private EventStreamsFeedService service;

    @Inject
    private AccessController accessController;

    @GET
    @Produces("application/vnd.event-source.event-streams+json")
    public Feed eventStreams(@DefaultValue("1") @PathParam("offset") int offset,
                             @PathParam("link") String link,
                             @PathParam("pageSize") int pageSize,
                             @Context UriInfo uriInfo) {
        accessController.checkAccessControl(headers);

        return service.feed(offset, valueOf(link), pageSize, uriInfo, new HashMap<String, Object>());
    }
}
