package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcBasedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.exception.VersionMismatchException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamManagerTest {

    private static final UUID STREAM_ID = randomUUID();
    private static final Long INITIAL_VERSION = 0L;
    private static final Long CURRENT_VERSION = 5L;
    private static final Long INVALID_VERSION = 8L;

    private final Logger logger = mock(Logger.class);
    private final JdbcBasedEventRepository eventRepository = mock(JdbcBasedEventRepository.class);
    private final EventAppender eventAppender = mock(EventAppender.class);
    private final Stream<JsonEnvelope> eventStream = mock(Stream.class);
    private final SystemEventService systemEventService = mock(SystemEventService.class);
    private final Enveloper enveloper = EnveloperFactory.createEnveloper();


    private static final long MAX_RETRY = 23L;

    private final EventStreamManager eventStreamManager = new EventStreamManager(
            eventAppender,
            MAX_RETRY,
            systemEventService,
            enveloper,
            eventRepository,
            logger
    );

    @Captor
    private ArgumentCaptor<JsonEnvelope> eventCaptor;

    @Captor
    private ArgumentCaptor<Long> versionCaptor;

    @Rule
    public ExpectedException expectedException = none();


    @Test
    public void shouldAppendToStream() throws Exception {
        when(eventRepository.getStreamPosition(STREAM_ID)).thenReturn(INITIAL_VERSION);

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
        when(eventRepository.getStreamSize(STREAM_ID)).thenReturn(CURRENT_VERSION);
        final long expectedVersion = CURRENT_VERSION + 1;

        final JsonEnvelope event = envelope().with(metadataWithRandomUUIDAndName()).build();
        eventStreamManager.appendAfter(STREAM_ID, Stream.of(event), CURRENT_VERSION);

        verify(eventAppender).append(event, STREAM_ID, expectedVersion);
    }

    @Test
    public void appendToStreamShouldReturnCurrentVersion() throws Exception {

        when(eventRepository.getStreamSize(STREAM_ID)).thenReturn(6L);

        long returnedVersion = eventStreamManager.append(STREAM_ID, Stream.of(
                envelope()
                        .with(metadataWithDefaults())
                        .build()));

        assertThat(returnedVersion, is(7L));

    }

    @Test
    public void appendAfterShouldReturnCurrentVersion() throws Exception {

        final long currentVersion = 4L;
        when(eventRepository.getStreamSize(STREAM_ID)).thenReturn(currentVersion);

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
    public void shouldThrowExceptionWhenEventsAreMissing() throws Exception {
        when(eventRepository.getStreamPosition(STREAM_ID)).thenReturn(CURRENT_VERSION);

        eventStreamManager.appendAfter(STREAM_ID, Stream.of(envelope().with(metadataWithDefaults()).build()), INVALID_VERSION);
    }

    @Test(expected = OptimisticLockingRetryException.class)
    public void shouldThrowExceptionWhenVersionAlreadyExists() throws Exception {
        when(eventRepository.getStreamSize(STREAM_ID)).thenReturn(CURRENT_VERSION + 1);

        eventStreamManager.appendAfter(STREAM_ID, Stream.of(envelope().with(metadataWithDefaults()).build()), CURRENT_VERSION);
    }

    @Test
    public void shouldReadStream() {
        when(eventRepository.getEventsByStreamId(STREAM_ID)).thenReturn(eventStream);

        Stream<JsonEnvelope> actualEnvelopeEventStream = eventStreamManager.read(STREAM_ID);

        assertThat(actualEnvelopeEventStream, equalTo(eventStream));
        verify(eventRepository).getEventsByStreamId(STREAM_ID);
    }

    @Test
    public void shouldReadStreamFromVersion() {
        when(eventRepository.getEventsByStreamIdFromPosition(STREAM_ID, CURRENT_VERSION)).thenReturn(eventStream);
        when(eventRepository.getStreamPosition(STREAM_ID)).thenReturn(CURRENT_VERSION);

        Stream<JsonEnvelope> actualEnvelopeEventStream = eventStreamManager.readFrom(STREAM_ID, CURRENT_VERSION);

        assertThat(actualEnvelopeEventStream, equalTo(eventStream));
        verify(eventRepository).getEventsByStreamIdFromPosition(STREAM_ID, CURRENT_VERSION);
    }

    @Test
    public void shouldGetCurrentVersion() {
        when(eventRepository.getStreamSize(STREAM_ID)).thenReturn(CURRENT_VERSION);
        final Long actualCurrentVersion = eventStreamManager.getSize(STREAM_ID);

        assertThat(actualCurrentVersion, equalTo(CURRENT_VERSION));
        verify(eventRepository).getStreamSize(STREAM_ID);
    }

    @Test
    public void shouldAppendNonConsecutively() throws Exception {
        when(eventRepository.getStreamSize(STREAM_ID)).thenReturn(CURRENT_VERSION);

        final JsonEnvelope event1 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event2 = envelope().with(metadataWithDefaults()).build();

        eventStreamManager.appendNonConsecutively(STREAM_ID, Stream.of(event1, event2));

        verify(eventAppender).append(event1, STREAM_ID, CURRENT_VERSION + 1);
        verify(eventAppender).append(event2, STREAM_ID, CURRENT_VERSION + 2);

    }

    @Test
    public void shouldReturnCurrentVersionWhenAppendingNonConsecutively() throws Exception {
        when(eventRepository.getStreamSize(STREAM_ID)).thenReturn(CURRENT_VERSION);

        final JsonEnvelope event1 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event2 = envelope().with(metadataWithDefaults()).build();

        long returnedVersion = eventStreamManager.appendNonConsecutively(STREAM_ID, Stream.of(event1, event2));
        assertThat(returnedVersion, is(CURRENT_VERSION + 2));

    }

    @Test
    public void shouldRetryWithNextVersionIdInCaseOfOptimisticLockException() throws Exception {
        setMaxRetries(20L);

        final long currentVersion = 6L;
        final long currentVersionAfterException = 11L;

        when(eventRepository.getStreamSize(STREAM_ID))
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

        setMaxRetries(20L);

        final long currentVersion = 6L;
        final long currentVersionAfterException = 11L;

        when(eventRepository.getStreamSize(STREAM_ID))
                .thenReturn(currentVersion).thenReturn(currentVersionAfterException);


        final JsonEnvelope event1 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event2 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event3 = envelope().with(metadataWithDefaults()).build();

        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event2, STREAM_ID, currentVersion + 2);

        eventStreamManager.appendNonConsecutively(STREAM_ID, Stream.of(event1, event2, event3));

        verify(logger).trace("Retrying appending to stream {}, with version {}", STREAM_ID, currentVersionAfterException + 1);
    }

    @Test
    public void shouldThrowExceptionAfterMaxNumberOfRetriesReached() throws Exception {

        setMaxRetries(2L);

        final long currentVersion = 6L;
        final long currentVersionAfterException1 = 11L;
        final long currentVersionAfterException2 = 12L;

        when(eventRepository.getStreamSize(STREAM_ID))
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

        setMaxRetries(2L);

        final long currentVersion = 6L;
        final long currentVersionAfterException1 = 11L;
        final long currentVersionAfterException2 = 12L;

        when(eventRepository.getStreamSize(STREAM_ID))
                .thenReturn(currentVersion)
                .thenReturn(currentVersionAfterException1)
                .thenReturn(currentVersionAfterException2);

        final JsonEnvelope event = envelope().with(metadataWithDefaults()).build();

        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event, STREAM_ID, currentVersion + 1);
        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event, STREAM_ID, currentVersionAfterException1 + 1);
        doThrow(OptimisticLockingRetryException.class).when(eventAppender).append(event, STREAM_ID, currentVersionAfterException2 + 1);

        try {
            eventStreamManager.appendNonConsecutively(STREAM_ID, Stream.of(event));
        } catch (final OptimisticLockingRetryException e) {
        }

        verify(logger).warn("Failed to append to stream {} due to concurrency issues, returning to handler.", STREAM_ID);
    }

    @Test
    public void shouldCloneStreamWithBlankVersions() throws EventStreamException {
        final JsonEnvelope event = buildEnvelope("test.events.event1");
        final JsonEnvelope systemEvent = buildEnvelope("system.events.cloned");
        when(eventRepository.getEventsByStreamId(STREAM_ID)).thenReturn(Stream.of(event));
        when(eventRepository.getStreamPosition(STREAM_ID)).thenReturn(0L);
        when(systemEventService.clonedEventFor(STREAM_ID)).thenReturn(systemEvent);

        final UUID clonedId = eventStreamManager.cloneAsAncestor(STREAM_ID);

        assertThat(clonedId, is(notNullValue()));
        assertThat(clonedId, is(not(STREAM_ID)));

        verify(eventAppender, times(2)).append(eventCaptor.capture(), eq(clonedId), versionCaptor.capture());
        final List<JsonEnvelope> clonedEvents = eventCaptor.getAllValues();

        assertThat(versionCaptor.getAllValues(), hasItems(1L, 2L));
        assertThat(clonedEvents, hasItems(systemEvent));
        final JsonEnvelope clonedEvent = clonedEvents.get(0);
        assertThat(clonedEvent.metadata().name(), is("test.events.event1"));
        assertThat(clonedEvent.metadata().version(), is(empty()));

        verify(eventRepository).markEventStreamActive(clonedId, false);
    }

    @Test
    public void shouldCloneAllEventsOnAStream() throws EventStreamException {
        final JsonEnvelope event1 = buildEnvelope("test.events.event1");
        final JsonEnvelope event2 = buildEnvelope("test.events.event2");
        final JsonEnvelope systemEvent = buildEnvelope("system.events.cloned");
        when(eventRepository.getEventsByStreamId(STREAM_ID)).thenReturn(Stream.of(event1, event2));
        when(eventRepository.getStreamPosition(STREAM_ID)).thenReturn(0L);
        when(systemEventService.clonedEventFor(STREAM_ID)).thenReturn(systemEvent);

        final UUID clonedId = eventStreamManager.cloneAsAncestor(STREAM_ID);

        assertThat(clonedId, is(notNullValue()));
        assertThat(clonedId, is(not(STREAM_ID)));

        verify(eventAppender, times(3)).append(eventCaptor.capture(), eq(clonedId), versionCaptor.capture());
        assertThat(versionCaptor.getAllValues(), hasItems(1L, 2L, 3L));

        verify(eventRepository).markEventStreamActive(clonedId, false);
    }

    @Test
    public void shouldClearEventStream() throws EventStreamException {
        eventStreamManager.clear(STREAM_ID);

        verify(eventRepository).clearEventsForStream(STREAM_ID);
        verifyNoMoreInteractions(eventRepository, eventAppender);
    }

    @Test
    public void shouldGetTheStreamPositionFromTheEventStreamManager() throws Exception {

        final UUID streamId = randomUUID();
        final long streamPosition = 23L;

        when(eventRepository.getStreamPosition(streamId)).thenReturn(streamPosition);

        assertThat(eventStreamManager.getStreamPosition(streamId), is(streamPosition));
    }

    private JsonEnvelope buildEnvelope(final String eventName) {
        return envelopeFrom(
                metadataBuilder().withId(randomUUID()).withStreamId(STREAM_ID).withName(eventName),
                createObjectBuilder().add("field", "value").build());
    }

    private void setMaxRetries(final long maxRetries) throws Exception {

        final Field maxRetry = eventStreamManager.getClass().getDeclaredField("maxRetry");

        maxRetry.setAccessible(true);
        maxRetry.set(eventStreamManager, maxRetries);
    }
}
