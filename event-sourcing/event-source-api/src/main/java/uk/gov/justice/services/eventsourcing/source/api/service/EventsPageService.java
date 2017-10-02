package uk.gov.justice.services.eventsourcing.source.api.service;

import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Position.first;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Position.head;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Position.positionOf;

import uk.gov.justice.services.eventsourcing.repository.jdbc.Direction;
import uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition;
import uk.gov.justice.services.eventsourcing.repository.jdbc.Position;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Page;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.PagingLinks.PagingLinksBuilder;
import uk.gov.justice.services.eventsourcing.source.api.feed.event.EventEntry;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

/**
 * Generator of paginated events
 */
@ApplicationScoped
public class EventsPageService {

    private static final int PATH_SEGMENT_EVENT_STREAMS = 0;

    private static final int PATH_SEGMENT_EVENT_STREAM_ID = 1;

    private static final long ZERO = 0L;

    @Inject
    private EventsService eventsService;

    public Page<EventEntry> pageEvents(final UUID streamId,
                                       final String position,
                                       final Direction direction,
                                       final long pageSize,
                                       final UriInfo uriInfo
    ) throws SQLException, MalformedURLException {

        final Position positionOf = positionOf(position);

        final List<EventEntry> entities = eventsService.events(streamId, positionOf, direction, pageSize);

        return pageEventsWithLinks(streamId, positionOf, pageSize, uriInfo, entities);
    }

    private Page<EventEntry> pageEventsWithLinks(final UUID streamId, final Position position, final long pageSize, final UriInfo uriInfo,
                                                 final List<EventEntry> entries) throws MalformedURLException {

        if (entries.isEmpty()) {
            return emptyPageWithLinks(pageSize, uriInfo);
        }

        final long maxSequenceId = max(entries);
        final long minSequenceId = min(entries);

        final boolean newEventsAvailable = !position.isHead() && eventsService.recordExists(streamId, maxSequenceId + 1);
        final boolean olderEventsAvailable = !position.isFirst() && eventsService.recordExists(streamId, minSequenceId - 1);

        final PagingLinksBuilder pagingLinksBuilder = new PagingLinksBuilder(
                fixedPositionPageHref(head(), BACKWARD, pageSize, uriInfo),
                fixedPositionPageHref(first(), FORWARD, pageSize, uriInfo));

        final Optional<URL> previous = newEventsAvailable ? pageHref(positionOf(maxSequenceId + 1), FORWARD, pageSize, uriInfo) : Optional.empty();

        final Optional<URL> next = olderEventsAvailable ? pageHref(positionOf(minSequenceId - 1), BACKWARD, pageSize, uriInfo) : Optional.empty();

        pagingLinksBuilder.previous(previous);

        pagingLinksBuilder.next(next);

        return new Page(entries, pagingLinksBuilder.build());
    }

    private Page<EventEntry> emptyPageWithLinks(long pageSize, UriInfo uriInfo) throws MalformedURLException {
        final PagingLinksBuilder pagingLinksBuilder = new PagingLinksBuilder(fixedPositionPageHref(head(), BACKWARD, pageSize, uriInfo),
                fixedPositionPageHref(first(), FORWARD, pageSize, uriInfo));
        return new Page<>(new ArrayList<>(), pagingLinksBuilder.build());
    }

    private Optional<URL> pageHref(final Position position,
                                   final Direction direction,
                                   final long pageSize,
                                   final UriInfo uriInfo) throws MalformedURLException {

        return Optional.of(new URL(uriInfo.getBaseUriBuilder()
                .path(uriInfo.getPathSegments().get(PATH_SEGMENT_EVENT_STREAMS).getPath())
                .path(uriInfo.getPathSegments().get(PATH_SEGMENT_EVENT_STREAM_ID).getPath())
                .path(position.getPosition())
                .path(urlDirection(position, direction))
                .path(String.valueOf(pageSize)).build()
                .toString()));
    }

    private URL fixedPositionPageHref(final Position position,
                                      final Direction direction,
                                      final long pageSize,
                                      final UriInfo uriInfo) throws MalformedURLException {

        return new URL(uriInfo.getBaseUriBuilder()
                .path(uriInfo.getPathSegments().get(PATH_SEGMENT_EVENT_STREAMS).getPath())
                .path(uriInfo.getPathSegments().get(PATH_SEGMENT_EVENT_STREAM_ID).getPath())
                .path(position.getPosition())
                .path(urlDirection(position, direction))
                .path(String.valueOf(pageSize)).build()
                .toString());
    }

    private String urlDirection(final Position position, final Direction direction) {
        final Direction firstDirection = position.getPosition().equals(FixedPosition.FIRST.getPosition()) ? FORWARD : direction;
        final Direction finalDirection = position.getPosition().equals(FixedPosition.HEAD.getPosition()) ? BACKWARD : firstDirection;
        return finalDirection.toString();
    }

    private long min(List<EventEntry> eventStreams) {
        return eventStreams.stream().mapToLong(EventEntry::getSequenceId).min().orElse(ZERO);
    }

    private long max(List<EventEntry> eventStreams) {
        return eventStreams.stream().mapToLong(EventEntry::getSequenceId).max().orElse(ZERO);
    }
}
