package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.STREAM;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.eventsourcing.publisher.core.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.core.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.exception.InvalidStreamVersionRuntimeException;
import uk.gov.justice.services.eventsourcing.source.core.exception.VersionMismatchException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

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

        eventStreamManager.append(STREAM_ID,
                singletonList(
                        envelope()
                                .with(metadataOf(ID_VALUE, NAME_VALUE))
                                .withPayloadOf(PAYLOAD_FIELD_VALUE, PAYLOAD_FIELD_NAME)
                                .build())
                        .stream());

        long expectedVersion = INITIAL_VERSION + 1;
        ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        ArgumentCaptor<UUID> streamIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Long> versionCaptor = ArgumentCaptor.forClass(Long.class);

        verify(eventRepository).store(envelopeArgumentCaptor.capture(), streamIdCaptor.capture(), versionCaptor.capture());
        verify(eventPublisher).publish(envelopeArgumentCaptor.capture());

        JsonEnvelope envelope = envelopeArgumentCaptor.getValue();
        Metadata metadata = envelope.metadata();
        assertThat(streamIdCaptor.getValue(), equalTo(STREAM_ID));
        assertThat(versionCaptor.getValue(), equalTo(expectedVersion));

        assertThat(metadata.version().get(), equalTo(expectedVersion));
        assertThat(metadata.id(), equalTo(ID_VALUE));
        assertThat(metadata.name(), equalTo(NAME_VALUE));
        assertThat(metadata.streamId().get(), equalTo(STREAM_ID));
        assertThat(metadata.version().get(), equalTo(expectedVersion));
        assertThat(metadata.asJsonObject().getJsonObject(STREAM).size(), equalTo(2));

        assertThat(envelope.payloadAsJsonObject().getString(PAYLOAD_FIELD_NAME), equalTo(PAYLOAD_FIELD_VALUE));

    }

    @Test(expected = EventStreamException.class)
    public void shouldThrowExceptionWhenEnvelopeContainsVersion() throws Exception {

        eventStreamManager.append(STREAM_ID, singletonList(envelope().with(metadataWithDefaults().withVersion(INITIAL_VERSION + 1)).build()).stream());

    }

    @Test
    public void shouldAppendToStreamFromVersion() throws Exception {
        when(eventRepository.getCurrentSequenceIdForStream(STREAM_ID)).thenReturn(CURRENT_VERSION);

        eventStreamManager.appendAfter(STREAM_ID, singletonList(envelope().with(metadataWithDefaults()).build()).stream(), CURRENT_VERSION);

        //FIXME: this test does not seem to be testing anything (no assertions)

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

}
