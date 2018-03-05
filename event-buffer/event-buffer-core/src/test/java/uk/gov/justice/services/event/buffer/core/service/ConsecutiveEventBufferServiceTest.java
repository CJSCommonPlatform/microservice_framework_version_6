package uk.gov.justice.services.event.buffer.core.service;

import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.empty;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;

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
    @SuppressWarnings("unused")
    private Logger logger;

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
        final String source = "source";
        final String eventName = "source.event.name";

        when(bufferInitialisationStrategy.initialiseBuffer(eq(streamId), eq(source))).thenReturn(4L);

        assertThat(
                bufferService.currentOrderedEventsWith(
                        envelope()
                                .with(metadataWithDefaults()
                                        .withName(eventName)
                                        .withStreamId(streamId)
                                        .withVersion(3L))
                                .build()),
                empty());


        assertThat(
                bufferService.currentOrderedEventsWith(
                        envelope()
                                .with(metadataWithDefaults()
                                        .withName(eventName)
                                        .withStreamId(streamId)
                                        .withVersion(4L))
                                .build()),
                empty());

        verifyZeroInteractions(streamBufferRepository);

    }

    @Test
    public void shouldReturnEventThatIsInCorrectOrder() {
        final UUID streamId = randomUUID();
        final String source = "source";
        final String eventName = "source.event.name";

        when(bufferInitialisationStrategy.initialiseBuffer(eq(streamId), eq(source))).thenReturn(4L);


        when(streamBufferRepository.findStreamByIdAndSource(streamId, source)).thenReturn(Stream.empty());

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withName(eventName)
                                .withStreamId(streamId)
                                .withVersion(5L))
                        .build();

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        assertThat(returnedEvents, contains(incomingEvent));
    }

    @Test
    public void shouldIncrementVersionOnIncomingEventInCorrectOrder() {
        final UUID streamId = randomUUID();

        final String source = "source";
        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withName(source)
                                .withStreamId(streamId)
                                .withVersion(5L))
                        .build();

        when(bufferInitialisationStrategy.initialiseBuffer(streamId, source)).thenReturn(4L);
        when(streamBufferRepository.findStreamByIdAndSource(streamId, source)).thenReturn(Stream.empty());

        bufferService.currentOrderedEventsWith(incomingEvent);
        verify(streamStatusRepository).update(new StreamStatus(streamId, 5L, source));

    }

    @Test
    public void shouldStoreEventIncomingNotInOrderAndReturnEmpty() {
        final String eventName = "source.events.something.happened";
        final String source = "source";
        final UUID streamId = randomUUID();

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withName(eventName)
                                .withStreamId(streamId)
                                .withVersion(6L))
                        .build();

        when(bufferInitialisationStrategy.initialiseBuffer(streamId, source)).thenReturn(4L);
        when(streamStatusRepository.findByStreamIdAndSource(streamId, source)).thenReturn(Optional.of(new StreamStatus(streamId, 4L, source)));

        when(jsonObjectEnvelopeConverter.asJsonString(incomingEvent)).thenReturn("someStringRepresentation");

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);

        verify(streamBufferRepository).insert(new StreamBufferEvent(streamId, 6L, "someStringRepresentation", source));
        assertThat(returnedEvents, empty());

    }

    @Test
    public void shouldReturnConsecutiveBufferedEventsIfIncomingEventFillsTheVersionGap() {

        final UUID streamId = randomUUID();
        final String source = "source";
        final String eventName = "source.event.name";

        when(bufferInitialisationStrategy.initialiseBuffer(eq(streamId), eq(source))).thenReturn(2L);

        when(streamBufferRepository.findStreamByIdAndSource(streamId, source)).thenReturn(
                Stream.of(new StreamBufferEvent(streamId, 4L, "someEventContent4", "source_4"),
                        new StreamBufferEvent(streamId, 5L, "someEventContent5", "source_5"),
                        new StreamBufferEvent(streamId, 6L, "someEventContent6", "source_6"),
                        new StreamBufferEvent(streamId, 8L, "someEventContent8", "source_8"),
                        new StreamBufferEvent(streamId, 9L, "someEventContent9", "source_9"),
                        new StreamBufferEvent(streamId, 10L, "someEventContent10", "source_10"),
                        new StreamBufferEvent(streamId, 11L, "someEventContent11", "source_11")));

        final JsonEnvelope bufferedEvent4 = envelope().build();
        final JsonEnvelope bufferedEvent5 = envelope().build();
        final JsonEnvelope bufferedEvent6 = envelope().build();

        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent4")).thenReturn(bufferedEvent4);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent5")).thenReturn(bufferedEvent5);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent6")).thenReturn(bufferedEvent6);

        final JsonEnvelope incomingEvent =
                envelope()
                        .with(metadataWithDefaults()
                                .withName(eventName)
                                .withStreamId(streamId)
                                .withVersion(3L))
                        .build();


        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        assertThat(returnedEvents, contains(incomingEvent, bufferedEvent4, bufferedEvent5, bufferedEvent6));
    }

    @Test
    public void shoulCloseSourceStreamOnConsecutiveStreamClose() {

        final UUID streamId = randomUUID();
        final String source = "source";
        final String eventName = "source.event.name";

        when(bufferInitialisationStrategy.initialiseBuffer(eq(streamId), eq(source))).thenReturn(2L);

        final StreamCloseSpy sourceStreamSpy = new StreamCloseSpy();

        when(streamBufferRepository.findStreamByIdAndSource(streamId, source)).thenReturn(
                Stream.of(new StreamBufferEvent(streamId, 4L, "someEventContent4", source),
                        new StreamBufferEvent(streamId, 8L, "someEventContent8", source)).onClose(sourceStreamSpy)
        );

        final JsonEnvelope bufferedEvent4 = mock(JsonEnvelope.class);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent4")).thenReturn(bufferedEvent4);

        final JsonEnvelope incomingEvent = envelope()
                .with(metadataWithDefaults()
                        .withName(eventName)
                        .withStreamId(streamId)
                        .withVersion(3L))
                .build();


        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        returnedEvents.close();

        assertThat(sourceStreamSpy.streamClosed(), is(true));
    }

    @Test
    public void shouldRemoveEventsFromBufferOnceStreamed() {

        final UUID streamId = randomUUID();
        final String source = "source";
        final String eventName = "source.event.name";
        when(bufferInitialisationStrategy.initialiseBuffer(eq(streamId), eq(source))).thenReturn(2L);


        final StreamBufferEvent event4 = new StreamBufferEvent(streamId, 4L, "someEventContent4", "source_1");
        final StreamBufferEvent event5 = new StreamBufferEvent(streamId, 5L, "someEventContent5", "source_2");
        final StreamBufferEvent event6 = new StreamBufferEvent(streamId, 6L, "someEventContent6", "source_3");

        when(streamBufferRepository.findStreamByIdAndSource(streamId, source)).thenReturn(
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
                                .withName(eventName)
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
