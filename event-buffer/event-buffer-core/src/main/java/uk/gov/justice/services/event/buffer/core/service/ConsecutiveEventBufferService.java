package uk.gov.justice.services.event.buffer.core.service;

import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;

import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.EventBufferEvent;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.EventBufferJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;
import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@ApplicationScoped
@Alternative
@Priority(2)
public class ConsecutiveEventBufferService implements EventBufferService {

    private static final long INITIAL_VERSION = 1l;

    @Inject
    private Logger logger;

    @Inject
    private EventBufferJdbcRepository streamBufferRepository;

    @Inject
    private SubscriptionJdbcRepository subscriptionJdbcRepository;

    @Inject
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    private BufferInitialisationStrategy bufferInitialisationStrategy;


    /**
     * Takes an incoming event and returns a stream of json envelopes. If the event is not
     * consecutive according to the stream_status repository then an empty stream is returned and
     * the incomingEvent is added to the event-buffer-repository. If the incomingEvent is
     * consecutive then it is returned as a stream with any consecutive events from the buffer. If
     * an event is the first to be processed for that streamId then the version value must be 1 or
     * the incomingEvent is added to the buffer and an empty stream is returned.
     *
     * @return stream of consecutive events
     */
    @Override
    public Stream<JsonEnvelope> currentOrderedEventsWith(final JsonEnvelope incomingEvent) {

        logger.trace("Message buffering for message: {}", incomingEvent);

        final UUID streamId = incomingEvent.metadata().streamId().orElseThrow(() -> new IllegalStateException("Event must have a a streamId "));
        final long incomingEventVersion = versionOf(incomingEvent);
        final String source = getSource(incomingEvent);

        final long currentVersion = bufferInitialisationStrategy.initialiseBuffer(streamId, source);

        if (incomingEventObsolete(incomingEventVersion, currentVersion)) {
            logger.warn("Message : {} is an obsolete version", incomingEvent);
            return Stream.empty();

        } else if (incomingEventNotInOrder(incomingEventVersion, currentVersion)) {
            logger.trace("Message : {} is not consecutive, adding to buffer", incomingEvent);
            addToBuffer(incomingEvent, streamId, incomingEventVersion);
            return Stream.empty();

        } else {
            logger.trace("Message : {} version is valid sending stream to dispatcher", incomingEvent);
            subscriptionJdbcRepository.update(new Subscription(streamId, incomingEventVersion, source));
            return bufferedEvents(streamId, incomingEvent, incomingEventVersion);
        }
    }

    private long versionOf(final JsonEnvelope event) {
        final long incomingEventVersion = event.metadata().position().orElseThrow(() -> new IllegalStateException("Event must have a version"));
        if (smallerThanInitial(incomingEventVersion)) {
            throw new IllegalStateException("Version cannot be zero");
        }
        return incomingEventVersion;
    }

    private boolean smallerThanInitial(final long incomingEventVersion) {
        return incomingEventVersion < INITIAL_VERSION;
    }

    private Stream<JsonEnvelope> bufferedEvents(final UUID streamId, final JsonEnvelope incomingEvent, final long incomingEventVersion) {
        final String source = getSource(incomingEvent);
        return concat(Stream.of(incomingEvent), consecutiveEventStreamFromBuffer(streamBufferRepository.findStreamByIdAndSource(streamId, source), incomingEventVersion)
                .peek(streamBufferEvent -> streamBufferRepository.remove(streamBufferEvent))
                .peek(streamBufferEvent -> subscriptionJdbcRepository.update(new Subscription(
                        streamBufferEvent.getStreamId(),
                        streamBufferEvent.getPosition(),
                        source)))
                .map(streamBufferEvent -> jsonObjectEnvelopeConverter.asEnvelope(streamBufferEvent.getEvent())));
    }

    private void addToBuffer(final JsonEnvelope incomingEvent, final UUID streamId, final Long incomingEventVersion) {
        streamBufferRepository.insert(
                new EventBufferEvent(streamId,
                        incomingEventVersion,
                        jsonObjectEnvelopeConverter.asJsonString(incomingEvent),
                        getSource(incomingEvent)));

    }

    private Stream<EventBufferEvent> consecutiveEventStreamFromBuffer(final Stream<EventBufferEvent> messageBuffer, final long currentVersion) {
        return stream(new ConsecutiveEventsSpliterator(messageBuffer, currentVersion), false).onClose(() -> messageBuffer.close());
    }

    private boolean incomingEventNotInOrder(final long incomingEventVersion, final long currentVersion) {
        return incomingEventVersion - currentVersion > 1;
    }

    private boolean incomingEventObsolete(final long incomingEventVersion, final long currentVersion) {
        return incomingEventVersion - currentVersion <= 0;
    }

    private String getSource(final JsonEnvelope incomingEvent) {
        return StringUtils.substringBefore(incomingEvent.metadata().name(), ".");
    }
}
