package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.IOException;
import java.io.StringReader;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EnvelopeTypeConverter {

    private ObjectMapper objectMapper;

    @Inject
    public EnvelopeTypeConverter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EnvelopeTypeConverter() {
    }

    public <R> Envelope<R> convert(final Envelope<?> envelope, final Class<R> clazz) throws IOException {

        if(envelope == null) { //Asynchronous methods return nulls / voids...
            return null;
        }

        if(clazz.isAssignableFrom(envelope.payload().getClass())) {
            return (Envelope<R>) envelope;
        }

        return envelopeFrom(envelope.metadata(), objectMapper.convertValue(envelope.payload(), clazz));
    }
}