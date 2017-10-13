package uk.gov.justice.services.eventsourcing.source.api.service;

import static java.util.Collections.emptyList;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.PagingLinks.PagingLinksBuilder.pagingLinksBuilder;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.sequence;

import uk.gov.justice.services.eventsourcing.repository.jdbc.Direction;
import uk.gov.justice.services.eventsourcing.source.api.service.PagingLinks.PagingLinksBuilder;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventStreamEntry;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventStreamService;
import uk.gov.justice.services.eventsourcing.source.api.service.core.Position;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

@ApplicationScoped
public class EventStreamPageService {

    private static final long ZERO = 0L;

    @Inject
    UrlLinkFactory urlLinkFactory;

    @Inject
    EventStreamService eventStreamService;

    @Inject
    PositionFactory positionFactory;

    public Page<EventStreamPageEntry> pageOfEventStream(final String positionValue,
                                                        final Direction direction,
                                                        final int pageSize,
                                                        final UriInfo uriInfo) throws MalformedURLException {
        final Position position = positionFactory.createPosition(positionValue);

        return pageOfEventStreamWithLinks(
                position,
                pageSize,
                uriInfo,
                eventStreamService.eventStream(position, direction, pageSize));
    }

    private Page<EventStreamPageEntry> pageOfEventStreamWithLinks(final Position position,
                                                                  final int pageSize,
                                                                  final UriInfo uriInfo,
                                                                  final List<EventStreamEntry> entries) throws MalformedURLException {
        if (entries.isEmpty()) {
            return emptyPageWithLinks(pageSize, uriInfo);
        }

        final PagingLinksBuilder pagingLinksBuilder =
                pagingLinksBuilder(
                        urlLinkFactory.createHeadEventStreamsUrlLink(pageSize, uriInfo),
                        urlLinkFactory.createFirstEventStreamsUrlLink(pageSize, uriInfo))
                        .withNext(nextUrlLink(position, pageSize, uriInfo, entries))
                        .withPrevious(previousUrlLink(position, pageSize, uriInfo, entries));

        return new Page<>(convertStreamIdToSelfLinks(entries, pageSize, uriInfo), pagingLinksBuilder.build());
    }

    private List<EventStreamPageEntry> convertStreamIdToSelfLinks(final List<EventStreamEntry> eventStreamEntries,
                                                                  final int pageSize,
                                                                  final UriInfo uriInfo) throws MalformedURLException {
        final List<EventStreamPageEntry> entriesWithSelfLinks = new ArrayList<>();

        for (EventStreamEntry eventStreamEntry : eventStreamEntries) {
            final URL selfLink = urlLinkFactory.createEventStreamSelfUrlLink(eventStreamEntry.getStreamId(), pageSize, uriInfo);
            final EventStreamPageEntry eventStreamEntryWithSelfLink = new EventStreamPageEntry(selfLink.toString(), eventStreamEntry.getSequenceNumber());
            entriesWithSelfLinks.add(eventStreamEntryWithSelfLink);
        }

        return entriesWithSelfLinks;
    }

    private Page<EventStreamPageEntry> emptyPageWithLinks(final int pageSize, final UriInfo uriInfo) throws MalformedURLException {

        final PagingLinksBuilder pagingLinksBuilder = pagingLinksBuilder(
                urlLinkFactory.createHeadEventStreamsUrlLink(pageSize, uriInfo),
                urlLinkFactory.createFirstEventStreamsUrlLink(pageSize, uriInfo));

        return new Page<>(emptyList(), pagingLinksBuilder.build());
    }

    private Optional<URL> previousUrlLink(final Position position,
                                          final int pageSize,
                                          final UriInfo uriInfo,
                                          final List<EventStreamEntry> entries) throws MalformedURLException {
        final long minSequenceId = min(entries);
        final boolean olderEventsAvailable = !position.isFirst() && eventStreamService.recordExists(minSequenceId - 1L);

        return olderEventsAvailable ? Optional.of(urlLinkFactory.createEventStreamUrlLink(sequence(minSequenceId - 1L), BACKWARD, pageSize, uriInfo)) : Optional.empty();
    }

    private Optional<URL> nextUrlLink(final Position position,
                                      final int pageSize,
                                      final UriInfo uriInfo,
                                      final List<EventStreamEntry> entries) throws MalformedURLException {
        final long maxSequenceId = max(entries);
        final boolean newEventsAvailable = !position.isHead() && eventStreamService.recordExists(maxSequenceId + 1L);

        return newEventsAvailable ? Optional.of(urlLinkFactory.createEventStreamUrlLink(sequence(maxSequenceId + 1L), FORWARD, pageSize, uriInfo)) : Optional.empty();
    }

    private long min(List<EventStreamEntry> eventStreams) {
        return eventStreams.stream().mapToLong(EventStreamEntry::getSequenceNumber).min().orElse(ZERO);
    }

    private long max(List<EventStreamEntry> eventStreams) {
        return eventStreams.stream().mapToLong(EventStreamEntry::getSequenceNumber).max().orElse(ZERO);
    }
}
