package uk.gov.justice.services.eventsourcing.source.api.resource;


import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.valueOf;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition.FIRST;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition.HEAD;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Page;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventsPageService;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonValue;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("event-streams/{streamId}/{position}/{direction}/{pageSize}")
public class EventPageResource {

    @Context
    HttpHeaders headers;

    @Inject
    private EventsPageService eventsPageService;

    @Inject
    private AccessController accessController;

    @Inject
    private ObjectMapperProducer mapperProducer;

    @Inject
    private ObjectToJsonValueConverter converter;

    @GET
    @Produces("application/vnd.event-source.events+json")
    public JsonValue events(
            @PathParam("streamId") final String streamId,
            @PathParam("position") final String position,
            @PathParam("direction") final String direction,
            @PathParam("pageSize") final long pageSize,
            @Context final UriInfo uriInfo) throws SQLException, MalformedURLException {

        if (HEAD.getPosition().equals(position) && FORWARD.toString().equals(direction)) {
            throw new BadRequestException("Cannot Request PREVIOUS not allowed when HEAD requested");
        }

        if (FIRST.getPosition().equals(position) && BACKWARD.toString().equals(direction)) {
            throw new BadRequestException("Cannot Request NEXT not allowed when FIRST requested");
        }

        accessController.checkAccessControl(headers);

        final Page page = eventsPageService.pageEvents(UUID.fromString(streamId), position, valueOf(direction), pageSize, uriInfo);

        return converter.convert(page);
    }
}