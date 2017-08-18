package uk.gov.justice.services.eventsourcing.source.api.resource;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventStreamsFeedService;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

@Path("event-streams")
public class EventStreamsFeedResource {

    @Context
    HttpHeaders headers;

    @Inject
    private EventStreamsFeedService service;

    @Inject
    private AccessController accessController;

    @GET
    @Produces("application/vnd.event-source.event-streams+json")
    public Feed eventStreams(@DefaultValue("1") @QueryParam("page") String page, @Context UriInfo uriInfo) {
        accessController.checkAccessControl(headers);
        return service.feed(page, uriInfo);
    }
}
