package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;

import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EnvelopePayloadTypeConverter {

    private ObjectMapper objectMapper;

    @Inject
    public EnvelopePayloadTypeConverter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EnvelopePayloadTypeConverter() {
    }

    public <R> Envelope<R> convert(final Envelope<?> envelope, final Class<R> clazz) {

        if(envelope == null) { //Asynchronous methods return nulls / voids...
            return null;
        }

        if(clazz.isAssignableFrom(envelope.payload().getClass())) {
            return (Envelope<R>) envelope;
        }

        return envelopeFrom(envelope.metadata(), objectMapper.convertValue(envelope.payload(), clazz));
    }
}