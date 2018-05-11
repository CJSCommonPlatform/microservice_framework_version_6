package uk.gov.justice.services.eventsourcing.source.core;


import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnvelopeEventStreamTest {

    public static final Long POSITION = 3L;
    public static final Long CURRENT_POSITION = 4L;
    public static final Long CURRENT_STREAM_POSITION = 8L;

    private static final UUID STREAM_ID = randomUUID();

    @Mock
    EventStreamManager eventStreamManager;

    @Mock
    Stream<JsonEnvelope> stream;

    @Captor
    ArgumentCaptor<Stream<JsonEnvelope>> streamCaptor;

    @Captor
    ArgumentCaptor<Long> versionCaptor;

    private EnvelopeEventStream envelopeEventStream;

    @Before
    public void setup() {
        envelopeEventStream = new EnvelopeEventStream(STREAM_ID, eventStreamManager);
        when(eventStreamManager.read(STREAM_ID)).thenReturn(Stream.of(
                envelope().with(metadataWithDefaults().withVersion(1L)).build(),
                envelope().with(metadataWithDefaults().withVersion(2L)).build(),
                envelope().with(metadataWithDefaults().withVersion(3L)).build(),
                envelope().with(metadataWithDefaults().withVersion(4L)).build()));
        when(eventStreamManager.readFrom(STREAM_ID, POSITION)).thenReturn(Stream.of(
                envelope().with(metadataWithDefaults().withVersion(3L)).build(),
                envelope().with(metadataWithDefaults().withVersion(4L)).build()
        ));
    }

    @Test
    public void shouldReturnStreamOfEnvelopes() throws Exception {
        envelopeEventStream.read();

        verify(eventStreamManager).read(STREAM_ID);
    }

    @Test
    public void shouldReturnStreamFromVersion() throws Exception {
        envelopeEventStream.readFrom(POSITION);

        verify(eventStreamManager).readFrom(STREAM_ID, POSITION);
    }

    @Test
    public void shouldAppendStream() throws Exception {
        envelopeEventStream.append(stream);

        verify(eventStreamManager).append(STREAM_ID, stream);
    }

    @Test
    public void shouldAppendStreamAfterVersion() throws Exception {
        envelopeEventStream.appendAfter(stream, POSITION);

        verify(eventStreamManager).appendAfter(STREAM_ID, stream, POSITION);
    }

    @Test
    public void shouldAppendWithNonConsecutiveTolerance() throws EventStreamException {
        envelopeEventStream.append(stream, Tolerance.NON_CONSECUTIVE);

        verify(eventStreamManager).appendNonConsecutively(STREAM_ID, stream);
    }

    @Test
    public void shouldAppendWithConsecutiveTolerance() throws EventStreamException {
        envelopeEventStream.append(stream, Tolerance.CONSECUTIVE);

        verify(eventStreamManager).append(STREAM_ID, stream);
    }


    @Test
    public void shouldReturnSize() throws Exception {
        envelopeEventStream.size();

        verify(eventStreamManager).getSize(STREAM_ID);
    }

    @Test
    public void shouldGetEventStreamId() throws Exception {
        final UUID actualId = envelopeEventStream.getId();

        assertThat(actualId, equalTo(STREAM_ID));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfReadTwice() {
        envelopeEventStream.read();
        envelopeEventStream.read();
    }

    @Test
    public void shouldAppendToLastReadVersion() throws Exception {
        final JsonEnvelope event = envelope().with(metadataWithDefaults()).build();
        final Stream<JsonEnvelope> events = Stream.of(event);

        envelopeEventStream.read().forEach(e -> {
        });
        envelopeEventStream.append(events);

        verify(eventStreamManager).appendAfter(eq(STREAM_ID), streamCaptor.capture(), eq(CURRENT_POSITION));
        final List<JsonEnvelope> appendedEvents = streamCaptor.getValue().collect(toList());
        assertThat(appendedEvents, hasSize(1));
        assertThat(appendedEvents.get(0), is(event));
    }

    @Test
    public void shouldAppendAnywhereIfStreamNotRead() throws Exception {
        final JsonEnvelope event = envelope().with(metadataWithDefaults()).build();
        final Stream<JsonEnvelope> events = Stream.of(event);

        envelopeEventStream.append(events);

        verify(eventStreamManager).append(eq(STREAM_ID), streamCaptor.capture());
        final List<JsonEnvelope> appendedEvents = streamCaptor.getValue().collect(toList());
        assertThat(appendedEvents, hasSize(1));
        assertThat(appendedEvents.get(0), is(event));
    }

    @Test
    public void shouldAllowTwoAppendsAfterRead() throws Exception {
        final JsonEnvelope event5 = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope event6 = envelope().with(metadataWithDefaults()).build();

        envelopeEventStream.read().forEach(e -> {
        });

        envelopeEventStream.append(Stream.of(event5));

        verify(eventStreamManager).appendAfter(eq(STREAM_ID), streamCaptor.capture(), versionCaptor.capture());
        final List<JsonEnvelope> appendedEvents1 = streamCaptor.getValue().collect(toList());
        assertThat(appendedEvents1, hasSize(1));
        assertThat(appendedEvents1.get(0), is(event5));
        assertThat(versionCaptor.getValue(), equalTo(CURRENT_POSITION));

        envelopeEventStream.append(Stream.of(event6));

        verify(eventStreamManager, times(2)).appendAfter(eq(STREAM_ID), streamCaptor.capture(), versionCaptor.capture());
        final List<JsonEnvelope> appendedEvents2 = streamCaptor.getValue().collect(toList());
        assertThat(appendedEvents2, hasSize(1));
        assertThat(appendedEvents2.get(0), is(event6));
        assertThat(versionCaptor.getValue(), equalTo(CURRENT_POSITION + 1));
    }

    @Test
    public void shouldGetStreamPosition(){
        when(eventStreamManager.getStreamPosition(STREAM_ID)).thenReturn(CURRENT_STREAM_POSITION);
        assertThat(envelopeEventStream.getPosition(), is(CURRENT_STREAM_POSITION));
        verify(eventStreamManager).getStreamPosition(STREAM_ID);
    }

    @Test
    public void shouldGetStreamSize(){
        when(eventStreamManager.getSize(STREAM_ID)).thenReturn(CURRENT_POSITION);
        assertThat(envelopeEventStream.size(), is(CURRENT_POSITION));
        verify(eventStreamManager).getSize(STREAM_ID);
    }

    @Test
    public void shouldGetCurrentVersionIfPresent() throws Exception {

        final long lastReadPosition = 23L;
        setField(envelopeEventStream, "lastReadPosition", of(lastReadPosition));

        assertThat(envelopeEventStream.getCurrentVersion(), is(lastReadPosition));
    }

    @Test
    public void shouldGetCurrentVersionFromEventStreamManagerIfNoReadPositionPresent() throws Exception {

        final long lastReadPosition = 23L;
        setField(envelopeEventStream, "lastReadPosition", empty());

        when(eventStreamManager.getSize(STREAM_ID)).thenReturn(lastReadPosition);

        assertThat(envelopeEventStream.getCurrentVersion(), is(lastReadPosition));
    }
}
