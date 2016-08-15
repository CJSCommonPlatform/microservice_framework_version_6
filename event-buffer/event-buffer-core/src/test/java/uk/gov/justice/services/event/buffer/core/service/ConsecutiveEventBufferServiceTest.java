package uk.gov.justice.services.event.buffer.core.service;


import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.empty;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferEvent;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ConsecutiveEventBufferServiceTest {

    @Mock
    private Logger LOGGER;

    @Mock
    private StreamStatusJdbcRepository streamStatusRepository;

    @Mock
    private StreamBufferJdbcRepository streamBufferRepository;

    @Mock
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @InjectMocks
    private ConsecutiveEventBufferService bufferService;


    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfNoStreamId() {

        bufferService.currentOrderedEventsWith(envelope().with(metadataWithDefaults().withVersion(1L)).build());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowZeroVersion() {
        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(envelope()
                .with(metadataWithDefaults()
                        .withVersion(0L)
                        .withStreamId(randomUUID()))
                .build());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowNullVersion() {

        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.empty());

        bufferService.currentOrderedEventsWith(envelope().with(metadataWithDefaults().withStreamId(streamId)).build());
    }

    @Test
    public void shouldTryInsertingZeroStatusInPostgres() {
        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 1l)));
        when(streamBufferRepository.streamById(streamId)).thenReturn(Stream.empty());

        bufferService.currentOrderedEventsWith(envelope().with(metadataWithDefaults().withStreamId(streamId).withVersion(2l)).build());

        verify(streamStatusRepository).tryInsertingInPostgres95(new StreamStatus(streamId, 0));

    }



    @Test
    public void shouldInsertZeroStatusAndUpdateToOneForInitialEventIfNoneAvailable() throws Exception {
        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.empty());

        final long version = 1l;
        bufferService.currentOrderedEventsWith(envelope().with(metadataWithDefaults().withStreamId(streamId).withVersion(version)).build());

        verify(streamStatusRepository).insert(new StreamStatus(streamId, 0));
        verify(streamStatusRepository).update(new StreamStatus(streamId, 1));

    }

    @Test
    public void shouldNotInsertStatusForNonInitialEvent() throws Exception {
        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.empty());

        final long version = 2l;
        bufferService.currentOrderedEventsWith(envelope().with(metadataWithDefaults().withStreamId(streamId).withVersion(version)).build());

        verify(streamStatusRepository, never()).insert(new StreamStatus(streamId, version));

    }


    @Test
    public void shouldReturnIncomingEventIfNoStatusAvailanle() throws Exception {
        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.empty());

        final long version = 1l;

        final JsonEnvelope incomingEvent = envelope().with(metadataWithDefaults().withStreamId(streamId).withVersion(version)).build();

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        assertThat(returnedEvents, contains(incomingEvent));


    }

    @Test
    public void shouldAddEventToBufferIfVersionNotOneAndStatusEmpty() throws Exception {
        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.empty());

        final long version = 2l;

        final JsonEnvelope incomingEvent = envelope().with(metadataWithDefaults().withStreamId(streamId).withVersion(version)).build();

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        assertThat(returnedEvents, empty());
        verify(streamBufferRepository).insert(new StreamBufferEvent(streamId, 2l, null));
    }

    @Test
    public void shouldIgnoreObsoleteEvent() {

        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 4l)));

        assertThat(
                bufferService.currentOrderedEventsWith(
                        envelope()
                                .with(metadataWithDefaults()
                                        .withStreamId(streamId)
                                        .withVersion(3l))
                                .build()),
                empty());


        assertThat(
                bufferService.currentOrderedEventsWith(
                        envelope()
                                .with(metadataWithDefaults()
                                        .withStreamId(streamId)
                                        .withVersion(4l))
                                .build()),
                empty());

        verifyZeroInteractions(streamBufferRepository);

    }

    @Test
    public void shouldReturnEventThatIsInCorrectOrder() {
        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 4l)));
        when(streamBufferRepository.streamById(streamId)).thenReturn(Stream.empty());

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withStreamId(streamId)
                                .withVersion(5l))
                        .build();

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        assertThat(returnedEvents, contains(incomingEvent));
    }

    @Test
    public void shouldIncrementVersionOnIncomingEventInCorrectOrder() {
        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 4l)));
        when(streamBufferRepository.streamById(streamId)).thenReturn(Stream.empty());

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withStreamId(streamId)
                                .withVersion(5l))
                        .build();

        bufferService.currentOrderedEventsWith(incomingEvent);
        verify(streamStatusRepository).update(new StreamStatus(streamId, 5l));

    }


    @Test
    public void shouldStoreEventIncomingNotInOrderAndReturnEmpty() {
        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 4l)));

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withStreamId(streamId)
                                .withVersion(6l))
                        .build();

        when(jsonObjectEnvelopeConverter.asJsonString(incomingEvent)).thenReturn("someStringRepresentation");

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);

        verify(streamBufferRepository).insert(new StreamBufferEvent(streamId, 6l, "someStringRepresentation"));
        assertThat(returnedEvents, empty());

    }

    @Test
    public void shouldReturnConsecutiveBufferedEventsIfIncomingEventFillsTheVersionGap() {

        final UUID streamId = randomUUID();

        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 2l)));

        when(streamBufferRepository.streamById(streamId)).thenReturn(
                Stream.of(new StreamBufferEvent(streamId, 4l, "someEventContent4"),
                        new StreamBufferEvent(streamId, 5l, "someEventContent5"),
                        new StreamBufferEvent(streamId, 6l, "someEventContent6"),
                        new StreamBufferEvent(streamId, 8l, "someEventContent8"),
                        new StreamBufferEvent(streamId, 9l, "someEventContent9"),
                        new StreamBufferEvent(streamId, 10l, "someEventContent10"),
                        new StreamBufferEvent(streamId, 11l, "someEventContent11")));

        final JsonEnvelope bufferedEvent4 = envelope().build();
        final JsonEnvelope bufferedEvent5 = envelope().build();
        final JsonEnvelope bufferedEvent6 = envelope().build();

        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent4")).thenReturn(bufferedEvent4);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent5")).thenReturn(bufferedEvent5);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent6")).thenReturn(bufferedEvent6);

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withStreamId(streamId)
                                .withVersion(3l))
                        .build();


        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        assertThat(returnedEvents, contains(incomingEvent, bufferedEvent4, bufferedEvent5, bufferedEvent6));


    }

    @Test
    public void shouldRemoveEventsFromBufferOnceStreamed() {

        final UUID streamId = randomUUID();
        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 2l)));

        final StreamBufferEvent event4 = new StreamBufferEvent(streamId, 4l, "someEventContent4");
        final StreamBufferEvent event5 = new StreamBufferEvent(streamId, 5l, "someEventContent5");
        final StreamBufferEvent event6 = new StreamBufferEvent(streamId, 6l, "someEventContent6");


        when(streamBufferRepository.streamById(streamId)).thenReturn(
                Stream.of(event4, event5, event6));

        final JsonEnvelope bufferedEvent4 = envelope().build();
        final JsonEnvelope bufferedEvent5 = envelope().build();
        final JsonEnvelope bufferedEvent6 = envelope().build();

        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent4")).thenReturn(bufferedEvent4);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent5")).thenReturn(bufferedEvent5);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent6")).thenReturn(bufferedEvent6);

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withStreamId(streamId)
                                .withVersion(3l))
                        .build();

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);

        assertThat(returnedEvents, contains(incomingEvent, bufferedEvent4, bufferedEvent5, bufferedEvent6));

        verify(streamBufferRepository).remove(event4);
        verify(streamBufferRepository).remove(event5);
        verify(streamBufferRepository).remove(event6);

    }
}
