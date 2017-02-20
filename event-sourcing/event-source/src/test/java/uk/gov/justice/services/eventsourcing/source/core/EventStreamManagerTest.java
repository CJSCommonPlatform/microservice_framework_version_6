package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.core.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.exception.VersionMismatchException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamManagerTest {

    private static final UUID STREAM_ID = UUID.randomUUID();
    private static final Long INITIAL_VERSION = 0L;
    private static final Long CURRENT_VERSION = 5L;
    private static final Long INVALID_VERSION = 8L;

    @Mock
    private Logger logger;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventAppender eventAppender;

    @Mock
    private Stream<JsonEnvelope> eventStream;

    @InjectMocks
    private EventStreamManager eventStreamManager;

    @Test
    public void shouldAppendToStream() throws Exception {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(INITIAL_VERSION);

        final JsonEnvelope event = envelope().with(metadataWithRandomUUIDAndName()).build();

        eventStreamManager.append(STREAM_ID, Stream.of(event));

        verify(eventAppender).append(event, STREAM_ID, INITIAL_VERSION + 1);

    }

    @Test(expected = EventStreamException.class)
    public void shouldThrowExceptionWhenEnvelopeContainsVersion() throws Exception {
        eventStreamManager.append(STREAM_ID, Stream.of(envelope().with(metadataWithDefaults().withVersion(INITIAL_VERSION + 1)).build()));
    }

    @Test
    public void shouldAppendToStreamFromVersion() throws Exception {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);
        final long expectedVersion = CURRENT_VERSION + 1;

        final JsonEnvelope event = envelope().with(metadataWithRandomUUIDAndName()).build();
        eventStreamManager.appendAfter(STREAM_ID, Stream.of(event), CURRENT_VERSION);

        verify(eventAppender).append(event, STREAM_ID, expectedVersion);
    }

    @Test
    public void appendToStreamShouldReturnCurrentVersion() throws Exception {

        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(6L);

        long returnedVersion = eventStreamManager.append(STREAM_ID, Stream.of(
                envelope()
                        .with(metadataWithDefaults())
                        .build()));

        assertThat(returnedVersion, is(7L));

    }

    @Test
    public void appendAfterShouldReturnCurrentVersion() throws Exception {

        final long currentVersion = 4L;
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(currentVersion);

        long returnedVersion = eventStreamManager.appendAfter(
                STREAM_ID,
                Stream.of(envelope().with(metadataWithDefaults()).build()),
                currentVersion);

        assertThat(returnedVersion, is(currentVersion + 1));

    }


    @Test(expected = EventStreamException.class)
    public void shouldThrowExceptionOnNullFromVersion() throws Exception {
        eventStreamManager.appendAfter(STREAM_ID, Stream.of(envelope().build()), null);
    }

    @Test(expected = VersionMismatchException.class)
    public void shouldThrowExceptionWhenFromVersionNotCorrect() throws Exception {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);

        eventStreamManager.appendAfter(STREAM_ID, Stream.of(envelope().with(metadataWithDefaults()).build()), INVALID_VERSION);
    }

    @Test
    public void shouldReadStream() {
        when(eventRepository.getByStreamId(STREAM_ID)).thenReturn(eventStream);

        Stream<JsonEnvelope> actualEnvelopeEventStream = eventStreamManager.read(STREAM_ID);

        assertThat(actualEnvelopeEventStream, equalTo(eventStream));
        verify(eventRepository).getByStreamId(STREAM_ID);
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

    @Test
    public void shouldAppendNonConsecutively() throws Exception {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);

        final JsonEnvelope event1 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event2 = envelope().with(metadataWithDefaults()).build();

        eventStreamManager.appendNonConsecutively(STREAM_ID, Stream.of(event1, event2));

        verify(eventAppender).append(event1, STREAM_ID, CURRENT_VERSION + 1);
        verify(eventAppender).append(event2, STREAM_ID, CURRENT_VERSION + 2);

    }

    @Test
    public void shouldReturnCurrentVersionWhenAppendingNonConsecutively() throws Exception {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);

        final JsonEnvelope event1 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event2 = envelope().with(metadataWithDefaults()).build();

        long returnedVersion = eventStreamManager.appendNonConsecutively(STREAM_ID, Stream.of(event1, event2));
        assertThat(returnedVersion, is(CURRENT_VERSION + 2));

    }

    @Test
    public void shouldRetryWithNextVersionIdInCaseOfOptimisticLockException() throws Exception {
        eventStreamManager.maxRetry = 20L;

        final long currentVersion = 6L;
        final long currentVersionAfterException = 11L;

        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID))
                .thenReturn(currentVersion).thenReturn(currentVersionAfterException);


        final JsonEnvelope event1 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event2 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event3 = envelope().with(metadataWithDefaults()).build();

        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event2, STREAM_ID, currentVersion + 2);

        eventStreamManager.appendNonConsecutively(STREAM_ID, Stream.of(event1, event2, event3));

        verify(eventAppender).append(event1, STREAM_ID, currentVersion + 1);
        verify(eventAppender).append(event2, STREAM_ID, currentVersion + 2);
        verify(eventAppender).append(event2, STREAM_ID, currentVersionAfterException + 1);
        verify(eventAppender).append(event3, STREAM_ID, currentVersionAfterException + 2);
    }

    @Test
    public void shouldTraceLogAnAttemptedRetry() throws Exception {
        eventStreamManager.maxRetry = 20L;

        final long currentVersion = 6L;
        final long currentVersionAfterException = 11L;

        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID))
                .thenReturn(currentVersion).thenReturn(currentVersionAfterException);


        final JsonEnvelope event1 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event2 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event3 = envelope().with(metadataWithDefaults()).build();

        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event2, STREAM_ID, currentVersion + 2);

        eventStreamManager.appendNonConsecutively(STREAM_ID, Stream.of(event1, event2, event3));

        verify(logger).trace("Retrying appending to stream {}, with version {}", STREAM_ID, currentVersionAfterException + 1);
    }


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionAfterMaxNumberOfRetriesReached() throws Exception {
        eventStreamManager.maxRetry = 2L;

        final long currentVersion = 6L;
        final long currentVersionAfterException1 = 11L;
        final long currentVersionAfterException2 = 12L;

        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID))
                .thenReturn(currentVersion)
                .thenReturn(currentVersionAfterException1)
                .thenReturn(currentVersionAfterException2);

        final JsonEnvelope event = envelope().with(metadataWithDefaults()).build();

        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event, STREAM_ID, currentVersion + 1);
        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event, STREAM_ID, currentVersionAfterException1 + 1);
        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event, STREAM_ID, currentVersionAfterException2 + 1);

        expectedException.expect(OptimisticLockingRetryException.class);

        eventStreamManager.appendNonConsecutively(STREAM_ID, Stream.of(event));

    }

    @Test
    public void shouldLogWarningAfterMaxNumberOfRetriesReached() throws Exception {
        eventStreamManager.maxRetry = 2L;

        final long currentVersion = 6L;
        final long currentVersionAfterException1 = 11L;
        final long currentVersionAfterException2 = 12L;

        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID))
                .thenReturn(currentVersion)
                .thenReturn(currentVersionAfterException1)
                .thenReturn(currentVersionAfterException2);

        final JsonEnvelope event = envelope().with(metadataWithDefaults()).build();

        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event, STREAM_ID, currentVersion + 1);
        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event, STREAM_ID, currentVersionAfterException1 + 1);
        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event, STREAM_ID, currentVersionAfterException2 + 1);

        try {
            eventStreamManager.appendNonConsecutively(STREAM_ID, Stream.of(event));
        } catch (OptimisticLockingRetryException e) {
        }

        verify(logger).warn("Failed to append to stream {} due to concurrency issues, returning to handler.", STREAM_ID);

    }
}
