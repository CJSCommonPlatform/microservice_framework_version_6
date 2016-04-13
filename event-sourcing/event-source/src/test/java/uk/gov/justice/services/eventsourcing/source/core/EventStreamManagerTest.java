package uk.gov.justice.services.eventsourcing.source.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.eventsourcing.publisher.core.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.core.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.exception.InvalidStreamVersionRuntimeException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamManagerTest {

    private static final UUID STREAM_ID = UUID.randomUUID();
    private static final Long CURRENT_VERSION = 5L;
    private static final Long INVALID_VERSION = 8L;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Stream<JsonEnvelope> eventStream;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Metadata metadata;

    private EventStreamManager eventStreamManager;

    @Before
    public void setup() {
        eventStreamManager = new EventStreamManager();
        eventStreamManager.eventPublisher = eventPublisher;
        eventStreamManager.eventRepository = eventRepository;
    }

    @Test
    public void shouldAppendToStream() throws Exception {
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.version()).thenReturn(Optional.empty());
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);

        eventStreamManager.append(STREAM_ID, Collections.singletonList(jsonEnvelope).stream());

        verify(eventRepository).store(jsonEnvelope, STREAM_ID, CURRENT_VERSION + 1);
        verify(eventPublisher).publish(jsonEnvelope);
    }

    @Test(expected = EventStreamException.class)
    public void shouldThrowExceptionWhenEnvelopeContainsVersion() throws Exception {
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.version()).thenReturn(Optional.of(CURRENT_VERSION + 1));

        eventStreamManager.append(STREAM_ID, Collections.singletonList(jsonEnvelope).stream());
    }

    @Test
    public void shouldAppendToStreamFromVersion() throws Exception {
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.version()).thenReturn(Optional.empty());
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);

        eventStreamManager.appendAfter(STREAM_ID, Collections.singletonList(jsonEnvelope).stream(), CURRENT_VERSION);

        verify(eventRepository).store(jsonEnvelope, STREAM_ID, CURRENT_VERSION + 1);
        verify(eventPublisher).publish(jsonEnvelope);
    }


    @Test(expected = EventStreamException.class)
    public void shouldThrowExceptionWhenStoreEventRequestFails() throws Exception {
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.version()).thenReturn(Optional.empty());
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);
        doThrow(StoreEventRequestFailedException.class).when(eventRepository).store(jsonEnvelope, STREAM_ID, CURRENT_VERSION + 1);

        eventStreamManager.append(STREAM_ID, Collections.singletonList(jsonEnvelope).stream());
    }

    @Test(expected = EventStreamException.class)
    public void shouldThrowExceptionOnNullFromVersion() throws Exception {
        eventStreamManager.appendAfter(STREAM_ID, Collections.singletonList(jsonEnvelope).stream(), null);
    }

    @Test(expected = EventStreamException.class)
    public void shouldThrowExceptionWhenFromVersionNotCorrect() throws Exception {
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.version()).thenReturn(Optional.empty());
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);

        eventStreamManager.appendAfter(STREAM_ID, Collections.singletonList(jsonEnvelope).stream(), INVALID_VERSION);
    }

    @Test
    public void shouldReadStream() {
        when(eventRepository.getByStreamId(STREAM_ID)).thenReturn(eventStream);

        Stream<JsonEnvelope> actualEnvelopeEventStream = eventStreamManager.read(STREAM_ID);

        assertThat(actualEnvelopeEventStream, equalTo(eventStream));
        verify(eventRepository).getByStreamId(STREAM_ID);
    }

    @Test(expected = InvalidStreamVersionRuntimeException.class)
    public void shouldThrowExceptionWhenReadingFromInvalidVersion() {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);

        eventStreamManager.readFrom(STREAM_ID, INVALID_VERSION);
    }

    @Test
    public void shouldReadStreamFromVersion() {
        when(eventRepository.getByStreamIdAndSequenceId(STREAM_ID, CURRENT_VERSION)).thenReturn(eventStream);
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);

        Stream<JsonEnvelope> actualEnvelopeEventStream = eventStreamManager.readFrom(STREAM_ID, CURRENT_VERSION);

        assertThat(actualEnvelopeEventStream, equalTo(eventStream));
        verify(eventRepository).getByStreamIdAndSequenceId(STREAM_ID, CURRENT_VERSION);
    }

    @Test
    public void shouldGetCurrentVersion() {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);
        Long actualCurrentVersion = eventStreamManager.getCurrentVersion(STREAM_ID);

        assertThat(actualCurrentVersion, equalTo(CURRENT_VERSION));
        verify(eventRepository).getCurrentSequenceIdForStream(STREAM_ID);
    }
//
//    @Test
//    public void shouldGetId() {
//        UUID actualId = eventStreamManager.getId();
//
//        assertThat(actualId, equalTo(STREAM_ID));
//    }
}