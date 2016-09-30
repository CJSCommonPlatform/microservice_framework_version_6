package uk.gov.justice.services.test.utils.core.helper;

import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;

public final class EventStreamMockHelper {

    private EventStreamMockHelper() {
    }

    /**
     * Call after completed operation on a Mock EventStream.  Verifies the append method call and
     * captures the Stream of JsonEnvelopes.
     *
     * @param eventStream the Mock EventStream
     * @return the Stream of JsonEnvelopes
     * @throws EventStreamException if an event could not be appended
     */
    @SuppressWarnings("unchecked")
    public static Stream<JsonEnvelope> verifyAppendAndGetArgumentFrom(final EventStream eventStream) throws EventStreamException {
        final ArgumentCaptor<Stream> argumentCaptor = forClass(Stream.class);
        verify(eventStream).append(argumentCaptor.capture());

        return ((Stream<JsonEnvelope>) argumentCaptor.getValue());
    }
}
