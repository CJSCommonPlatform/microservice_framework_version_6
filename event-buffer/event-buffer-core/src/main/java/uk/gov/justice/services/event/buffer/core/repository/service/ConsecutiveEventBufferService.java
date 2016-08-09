package uk.gov.justice.services.event.buffer.core.repository.service;

import org.slf4j.Logger;

import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.buffer.core.repository.spliterator.EventBufferSpliterator;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferEvent;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import static java.lang.String.format;
import static uk.gov.justice.services.messaging.logging.JsonEnvelopeLoggerHelper.toEnvelopeTraceString;

@ApplicationScoped
@Alternative
@Priority(2)
public class ConsecutiveEventBufferService implements EventBufferService {

    private static final long INITIAL_VERSION = 1L;

    @Inject
    private Logger logger;

    @Inject
    private StreamBufferJdbcRepository streamBufferRepository;

    @Inject
    private StreamStatusJdbcRepository streamStatusRepository;

    @Inject
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;


    /**
     *
     * Takes an incoming event and returns a stream of json envelopes. If the event is not consecutive
     * according to the stream_status repository then an empty stream is returned and the incomingEvent
     * is added to the event-buffer-repository. If the incomingEvent is consecutive then it is returned
     * as a stream with any consecutive events from the buffer. If an event is the first to be processed
     * for that streamId then the version value must be 1 or the incomingEvent is added to the buffer
     * and an empty stream is returned.
     *
     * @param incomingEvent
     * @return streamOfConsecutiveEvents
     */
    @Override
    public Stream<JsonEnvelope> currentOrderedEventsWith(final JsonEnvelope incomingEvent) {

        logger.trace("Message buffering for message: {}", toEnvelopeTraceString(incomingEvent));

        final UUID streamId = incomingEvent.metadata().streamId().orElseThrow(() -> new IllegalStateException("Event must have a a streamId "));
        final Long incomingEventVersion = incomingEvent.metadata().version().orElseThrow(() -> new IllegalStateException("Event must have a version"));

        if(incomingEventVersion == 0L) {
            throw new IllegalStateException(format("Version cannot be zero"));
        }

        final Optional<StreamStatus> currentStatus = streamStatusRepository.findByStreamId(streamId);
        if (currentStatus.isPresent()) {
            final Long currentVersion = currentStatus.get().getVersion();
            if (incomingEventObsolete(incomingEventVersion, currentVersion)) {
                logger.trace("Message : {} is an obsolete version", toEnvelopeTraceString(incomingEvent));
                return Stream.empty();

            } else if (incomingEventNotInOrder(incomingEventVersion, currentVersion)) {
                logger.trace("Message : {} is not consecutive, adding to buffer", toEnvelopeTraceString(incomingEvent));
                return addToBufferAndReturnEmptyStream(incomingEvent, streamId, incomingEventVersion);

            } else {
                logger.trace("Message : {} version is valid sending stream to dispatcher", toEnvelopeTraceString(incomingEvent));
                return streamOfIncomingAndBufferedEvents(incomingEvent, streamId, incomingEventVersion);

            }
        } else {
            if(incomingEventVersion == INITIAL_VERSION) {
                logger.trace("Message : {} is a new streamId registering with stream_status repo and sending to dispatcher", toEnvelopeTraceString(incomingEvent));
                streamStatusRepository.insert(new StreamStatus(streamId, incomingEventVersion));
                return Stream.of(incomingEvent);
            } else {
                logger.trace("Message : {} is a new streamId but version is not INITIAL_VERSION so adding to buffer", toEnvelopeTraceString(incomingEvent));
                return addToBufferAndReturnEmptyStream(incomingEvent, streamId, incomingEventVersion);
            }
        }

    }

    private Stream<JsonEnvelope> streamOfIncomingAndBufferedEvents(final JsonEnvelope incomingEvent, final UUID streamId, final Long incomingEventVersion) {
        streamStatusRepository.update(new StreamStatus(streamId, incomingEventVersion));
        return bufferedEvents(Stream.of(incomingEvent), streamBufferRepository.streamById(streamId), incomingEventVersion);
    }

    private Stream<JsonEnvelope> addToBufferAndReturnEmptyStream(final JsonEnvelope incomingEvent, final UUID streamId, final Long incomingEventVersion) {
        streamBufferRepository.insert(
                new StreamBufferEvent(streamId,
                        incomingEventVersion,
                        jsonObjectEnvelopeConverter.asJsonString(incomingEvent)));

        return Stream.empty();
    }

    private Stream<JsonEnvelope> bufferedEvents(final Stream<JsonEnvelope> incomingMessage, final Stream<StreamBufferEvent> messageBuffer, final long currentVersion) {

        return Stream.concat(incomingMessage, consecutiveEventStreamFromBuffer(messageBuffer, currentVersion)
                .peek(streamBufferEvent -> streamBufferRepository.remove(streamBufferEvent))
                .peek(streamBufferEvent -> streamStatusRepository.update(new StreamStatus(streamBufferEvent.getStreamId(), streamBufferEvent.getVersion())))
                .map(streamBufferEvent -> jsonObjectEnvelopeConverter.asEnvelope(streamBufferEvent.getEvent())));
    }

    private Stream<StreamBufferEvent> consecutiveEventStreamFromBuffer(final Stream<StreamBufferEvent> messageBuffer, final long currentVersion) {
        return StreamSupport.stream(new EventBufferSpliterator(currentVersion, messageBuffer.iterator()).invoke(), false);
    }

    private boolean incomingEventNotInOrder(final Long incomingEventVersion, final Long currentVersion) {
        return incomingEventVersion - currentVersion > 1;
    }

    private boolean incomingEventObsolete(final Long incomingEventVersion, final Long currentVersion) {
        return incomingEventVersion - currentVersion <= 0;
    }


}
