package uk.gov.justice.services.event.buffer.core.service;


import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.empty;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;

import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferEvent;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.test.utils.common.stream.StreamCloseSpy;

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

    @Mock
    private BufferInitialisationStrategy bufferInitialisationStrategy;

    @InjectMocks
    private ConsecutiveEventBufferService bufferService;


    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfNoStreamId() {

        bufferService.currentOrderedEventsWith(envelope().with(metadataWithRandomUUIDAndName().withVersion(1L)).build());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowZeroVersion() {
        bufferService.currentOrderedEventsWith(envelope()
                .with(metadataWithDefaults()
                        .withVersion(0L)
                        .withStreamId(randomUUID()))
                .build());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowNullVersion() {

        bufferService.currentOrderedEventsWith(envelope().with(metadataWithRandomUUIDAndName().withStreamId(randomUUID())).build());
    }

    @Test
    public void shouldIgnoreObsoleteEvent() {

        final UUID streamId = randomUUID();

        when(bufferInitialisationStrategy.initialiseBuffer(eq(streamId))).thenReturn(4l);

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

        when(bufferInitialisationStrategy.initialiseBuffer(eq(streamId))).thenReturn(4l);


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

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withStreamId(streamId)
                                .withVersion(5l))
                        .build();

        when(bufferInitialisationStrategy.initialiseBuffer(streamId)).thenReturn(4l);
        when(streamBufferRepository.streamById(streamId)).thenReturn(Stream.empty());

        bufferService.currentOrderedEventsWith(incomingEvent);
        verify(streamStatusRepository).update(new StreamStatus(streamId, 5l));

    }

    @Test
    public void shouldStoreEventIncomingNotInOrderAndReturnEmpty() {
        final UUID streamId = randomUUID();

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withStreamId(streamId)
                                .withVersion(6l))
                        .build();

        when(bufferInitialisationStrategy.initialiseBuffer(streamId)).thenReturn(4l);
        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 4l)));


        when(jsonObjectEnvelopeConverter.asJsonString(incomingEvent)).thenReturn("someStringRepresentation");

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);

        verify(streamBufferRepository).insert(new StreamBufferEvent(streamId, 6l, "someStringRepresentation"));
        assertThat(returnedEvents, empty());

    }

    @Test
    public void shouldReturnConsecutiveBufferedEventsIfIncomingEventFillsTheVersionGap() {

        final UUID streamId = randomUUID();

        when(bufferInitialisationStrategy.initialiseBuffer(eq(streamId))).thenReturn(2l);

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
    public void shoulCloseSourceStreamOnConsecutiveStreamClose() {

        final UUID streamId = randomUUID();

        when(bufferInitialisationStrategy.initialiseBuffer(eq(streamId))).thenReturn(2l);

        final StreamCloseSpy sourceStreamSpy = new StreamCloseSpy();

        when(streamBufferRepository.streamById(streamId)).thenReturn(
                Stream.of(new StreamBufferEvent(streamId, 4l, "someEventContent4"),
                        new StreamBufferEvent(streamId, 8l, "someEventContent8")).onClose(sourceStreamSpy)
        );

        final JsonEnvelope bufferedEvent4 = envelope().build();

        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent4")).thenReturn(bufferedEvent4);

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withStreamId(streamId)
                                .withVersion(3l))
                        .build();


        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        returnedEvents.close();

        assertThat(sourceStreamSpy.streamClosed(), is(true));


    }


    @Test
    public void shouldRemoveEventsFromBufferOnceStreamed() {

        final UUID streamId = randomUUID();
        when(bufferInitialisationStrategy.initialiseBuffer(eq(streamId))).thenReturn(2l);


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
                                .withVersion(3L))
                        .build();

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);

        assertThat(returnedEvents, contains(incomingEvent, bufferedEvent4, bufferedEvent5, bufferedEvent6));

        verify(streamBufferRepository).remove(event4);
        verify(streamBufferRepository).remove(event5);
        verify(streamBufferRepository).remove(event6);

    }
}
