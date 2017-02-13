package uk.gov.justice.services.eventsourcing.source.core;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;

import uk.gov.justice.services.eventsourcing.publisher.core.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.core.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.exception.InvalidStreamVersionRuntimeException;
import uk.gov.justice.services.eventsourcing.source.core.exception.VersionMismatchException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamManagerTest {

    private static final UUID STREAM_ID = UUID.randomUUID();
    private static final Long INITIAL_VERSION = 0L;
    private static final Long CURRENT_VERSION = 5L;
    private static final Long INVALID_VERSION = 8L;
    private static final UUID ID_VALUE = UUID.randomUUID();
    private static final String NAME_VALUE = "test.event.something-happened";
    private static final String PAYLOAD_FIELD_NAME = "payloadField";
    private static final String PAYLOAD_FIELD_VALUE = "payloadValue";

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Stream<JsonEnvelope> eventStream;

    private EventStreamManager eventStreamManager;

    @Before
    public void setup() {
        eventStreamManager = new EventStreamManager();
        eventStreamManager.eventPublisher = eventPublisher;
        eventStreamManager.eventRepository = eventRepository;
    }

    @Test
    public void shouldAppendToStream() throws Exception {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(INITIAL_VERSION);

        eventStreamManager.append(STREAM_ID, Stream.of(
                envelope()
                        .with(metadataOf(ID_VALUE, NAME_VALUE))
                        .withPayloadOf(PAYLOAD_FIELD_VALUE, PAYLOAD_FIELD_NAME)
                        .build()));

        long expectedVersion = INITIAL_VERSION + 1;
        ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(eventRepository).store(envelopeArgumentCaptor.capture());
        verify(eventPublisher).publish(envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata()
                        .withVersion(expectedVersion)
                        .withId(ID_VALUE)
                        .withName(NAME_VALUE)
                        .withStreamId(STREAM_ID),
                payloadIsJson(
                        withJsonPath(format("$.%s", PAYLOAD_FIELD_NAME), equalTo(PAYLOAD_FIELD_VALUE))
                )));
    }



    @Test(expected = EventStreamException.class)
    public void shouldThrowExceptionWhenEnvelopeContainsVersion() throws Exception {
        eventStreamManager.append(STREAM_ID, singletonList(envelope().with(metadataWithDefaults().withVersion(INITIAL_VERSION + 1)).build()).stream());
    }


    @Test
    public void shouldAppendToStreamFromVersion() throws Exception {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);
        final long expectedVersion = CURRENT_VERSION + 1;

        eventStreamManager.appendAfter(STREAM_ID,
                singletonList(
                        envelope()
                                .with(metadataOf(ID_VALUE, NAME_VALUE))
                                .withPayloadOf(PAYLOAD_FIELD_VALUE, PAYLOAD_FIELD_NAME)
                                .build())
                        .stream(), CURRENT_VERSION);

        final ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(eventRepository).store(envelopeArgumentCaptor.capture());
        verify(eventPublisher).publish(envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata()
                        .withVersion(expectedVersion)
                        .withId(ID_VALUE)
                        .withName(NAME_VALUE)
                        .withStreamId(STREAM_ID),
                payloadIsJson(
                        withJsonPath(format("$.%s", PAYLOAD_FIELD_NAME), equalTo(PAYLOAD_FIELD_VALUE))
                )));
    }

    @Test
    public void appendToStreamShouldReturnCurrentVersion() throws Exception {

        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(6L);

        long returnedVersion = eventStreamManager.append(STREAM_ID, Stream.of(
                envelope()
                        .with(metadataWithDefaults())
                        .build()));

        assertThat(returnedVersion, is(7l));

    }

    @Test
    public void appendAfterShouldReturnCurrentVersion() throws Exception {

        final long currentVersion = 4L;
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(currentVersion);

        long returnedVersion = eventStreamManager.appendAfter(STREAM_ID, Stream.of(
                envelope()
                        .with(metadataWithDefaults())
                        .build()), currentVersion);

        assertThat(returnedVersion, is(currentVersion + 1));

    }

    @Test(expected = EventStreamException.class)
    public void shouldThrowExceptionWhenStoreEventRequestFails() throws Exception {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);
        doThrow(StoreEventRequestFailedException.class).when(eventPublisher).publish(Matchers.any());

        eventStreamManager.append(STREAM_ID, singletonList(envelope().with(metadataWithDefaults()).build()).stream());
    }

    @Test(expected = EventStreamException.class)
    public void shouldThrowExceptionOnNullFromVersion() throws Exception {
        eventStreamManager.appendAfter(STREAM_ID, singletonList(envelope().build()).stream(), null);
    }

    @Test(expected = VersionMismatchException.class)
    public void shouldThrowExceptionWhenFromVersionNotCorrect() throws Exception {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);

        eventStreamManager.appendAfter(STREAM_ID, singletonList(envelope().with(metadataWithDefaults()).build()).stream(), INVALID_VERSION);
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

}
