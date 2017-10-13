package uk.gov.justice.services.eventsourcing.source.api.service;

import static java.util.Collections.emptyList;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.PagingLinks.PagingLinksBuilder.pagingLinksBuilder;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.sequence;

import uk.gov.justice.services.eventsourcing.repository.jdbc.Direction;
import uk.gov.justice.services.eventsourcing.source.api.service.PagingLinks.PagingLinksBuilder;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventEntry;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventsService;
import uk.gov.justice.services.eventsourcing.source.api.service.core.Position;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionFactory;

import java.net.MalformedURLException;
import java.net.URL;
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

    private static final long ZERO = 0L;

    @Inject
    EventsService eventsService;

    @Inject
    UrlLinkFactory urlLinkFactory;

    @Inject
    PositionFactory positionFactory;

    public Page<EventEntry> pageEvents(final UUID streamId,
                                       final String positionValue,
                                       final Direction direction,
                                       final int pageSize,
                                       final UriInfo uriInfo) throws MalformedURLException {
        final Position position = positionFactory.createPosition(positionValue);

        return pageEventsWithLinks(
                streamId,
                position,
                pageSize,
                uriInfo,
                eventsService.events(streamId, position, direction, pageSize));
    }

    private Page<EventEntry> pageEventsWithLinks(final UUID streamId,
                                                 final Position position,
                                                 final int pageSize,
                                                 final UriInfo uriInfo,
                                                 final List<EventEntry> entries) throws MalformedURLException {

        if (entries.isEmpty()) {
            return emptyPageWithLinks(pageSize, uriInfo);
        }

        final PagingLinksBuilder pagingLinksBuilder = pagingLinksBuilder(
                urlLinkFactory.createHeadEventsUrlLink(pageSize, uriInfo),
                (urlLinkFactory.createFirstEventsUrlLink(pageSize, uriInfo)))
                .withNext(nextUrlLink(streamId, position, pageSize, uriInfo, entries))
                .withPrevious(previousUrlLink(streamId, position, pageSize, uriInfo, entries));

        return new Page<>(entries, pagingLinksBuilder.build());
    }

    private Page<EventEntry> emptyPageWithLinks(final int pageSize, final UriInfo uriInfo) throws MalformedURLException {

        return new Page<>(emptyList(),
                pagingLinksBuilder(urlLinkFactory.createHeadEventsUrlLink(pageSize, uriInfo), urlLinkFactory.createFirstEventsUrlLink(pageSize, uriInfo))
                        .build());
    }

    private Optional<URL> previousUrlLink(final UUID streamId,
                                          final Position position,
                                          final int pageSize,
                                          final UriInfo uriInfo,
                                          final List<EventEntry> entries) throws MalformedURLException {
        final long minSequenceId = min(entries);
        final boolean olderEventsAvailable = !position.isFirst() && eventsService.recordExists(streamId, minSequenceId - 1);

        return olderEventsAvailable ?
                Optional.of(urlLinkFactory.createEventsUrlLink(sequence(minSequenceId - 1), BACKWARD, pageSize, uriInfo)) :
                Optional.empty();
    }

    private Optional<URL> nextUrlLink(final UUID streamId,
                                      final Position position,
                                      final int pageSize,
                                      final UriInfo uriInfo,
                                      final List<EventEntry> entries) throws MalformedURLException {
        final long maxSequenceId = max(entries);
        final boolean newEventsAvailable = !position.isHead() && eventsService.recordExists(streamId, maxSequenceId + 1);

        return newEventsAvailable ?
                Optional.of(urlLinkFactory.createEventsUrlLink(sequence(maxSequenceId + 1), FORWARD, pageSize, uriInfo)) :
                Optional.empty();
    }

    private long min(List<EventEntry> eventStreams) {
        return eventStreams.stream().mapToLong(EventEntry::getSequenceId).min().orElse(ZERO);
    }

    private long max(List<EventEntry> eventStreams) {
        return eventStreams.stream().mapToLong(EventEntry::getSequenceId).max().orElse(ZERO);
    }
}
