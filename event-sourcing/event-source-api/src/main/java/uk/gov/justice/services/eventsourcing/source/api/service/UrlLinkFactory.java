package uk.gov.justice.services.eventsourcing.source.api.service;

import static java.lang.String.valueOf;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.first;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.head;

import uk.gov.justice.services.eventsourcing.repository.jdbc.Direction;
import uk.gov.justice.services.eventsourcing.source.api.service.core.Position;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionValueFactory;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

public class UrlLinkFactory {

    private static final int PATH_SEGMENT_EVENT_STREAMS = 0;

    private static final int PATH_SEGMENT_EVENT_STREAM_ID = 1;

    @Inject
    PositionValueFactory positionValueFactory;

    public URL createHeadEventStreamsUrlLink(final int pageSize, final UriInfo uriInfo) throws MalformedURLException {
        return createEventStreamUrlLink(head(), BACKWARD, pageSize, uriInfo);
    }

    public URL createFirstEventStreamsUrlLink(final int pageSize, final UriInfo uriInfo) throws MalformedURLException {
        return createEventStreamUrlLink(first(), FORWARD, pageSize, uriInfo);
    }

    public URL createHeadEventsUrlLink(final int pageSize, final UriInfo uriInfo) throws MalformedURLException {
        return createEventsUrlLink(head(), BACKWARD, pageSize, uriInfo);
    }

    public URL createFirstEventsUrlLink(final int pageSize, final UriInfo uriInfo) throws MalformedURLException {
        return createEventsUrlLink(first(), FORWARD, pageSize, uriInfo);
    }

    public URL createEventStreamUrlLink(final Position position,
                                        final Direction direction,
                                        final int pageSize,
                                        final UriInfo uriInfo) throws MalformedURLException {
        return new URL(uriInfo.getBaseUriBuilder()
                .path(uriInfo.getPathSegments().get(PATH_SEGMENT_EVENT_STREAMS).getPath())
                .path(positionValueFactory.getPositionValue(position))
                .path(urlDirection(position, direction))
                .path(valueOf(pageSize)).build()
                .toString());
    }

    public URL createEventStreamSelfUrlLink(final String streamId,
                                            final int pageSize,
                                            final UriInfo uriInfo) throws MalformedURLException {
        return createUrl(head(), streamId, BACKWARD, pageSize, uriInfo);
    }

    public URL createEventsUrlLink(final Position position,
                                   final Direction direction,
                                   final int pageSize,
                                   final UriInfo uriInfo) throws MalformedURLException {
        final String streamId = uriInfo.getPathSegments().get(PATH_SEGMENT_EVENT_STREAM_ID).getPath();
        return createUrl(position, streamId, direction, pageSize, uriInfo);
    }

    private URL createUrl(final Position position,
                          final String streamId,
                          final Direction direction,
                          final int pageSize,
                          final UriInfo uriInfo) throws MalformedURLException {
        return new URL(uriInfo.getBaseUriBuilder()
                .path(uriInfo.getPathSegments().get(PATH_SEGMENT_EVENT_STREAMS).getPath())
                .path(streamId)
                .path(positionValueFactory.getPositionValue(position))
                .path(urlDirection(position, direction))
                .path(valueOf(pageSize)).build()
                .toString());
    }

    private String urlDirection(final Position position, final Direction direction) {
        final Direction firstDirection = position.equals(first()) ? FORWARD : direction;
        final Direction finalDirection = position.equals(head()) ? BACKWARD : firstDirection;
        return finalDirection.toString();
    }
}
