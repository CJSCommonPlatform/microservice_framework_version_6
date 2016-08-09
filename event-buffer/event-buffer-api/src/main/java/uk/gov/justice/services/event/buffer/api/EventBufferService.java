package uk.gov.justice.services.event.buffer.api;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

public interface EventBufferService {

    Stream<JsonEnvelope> currentOrderedEventsWith(final JsonEnvelope incomingEvent);
}