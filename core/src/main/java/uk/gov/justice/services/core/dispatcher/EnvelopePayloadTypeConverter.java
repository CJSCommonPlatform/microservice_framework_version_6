package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;

import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *   Converts Envelope with payload of T to Envelope with payload of R where
 *   T can be converted to R using a jackson object mapper
 */
public class EnvelopePayloadTypeConverter {

    private ObjectMapper objectMapper;

    @Inject
    public EnvelopePayloadTypeConverter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EnvelopePayloadTypeConverter() {
    }

    /**
     * Converts Envelope with payload of T to Envelope with payload of R where
     * T can be converted to R using a jackson object mapper
     *
     * @param envelope to be converted
     * @param clazz of the new payload type
     * @return an envelope of type clazz
     */
    public <R> Envelope<R> convert(final Envelope<?> envelope, final Class<R> clazz) {

        if(envelope == null) { //Asynchronous methods return nulls / voids...
            return null;
        }

        if(envelope.payload() == null){
            return envelopeFrom(envelope.metadata(), null);
        }

        if(clazz.isAssignableFrom(envelope.payload().getClass())) {
            return (Envelope<R>) envelope;
        }

        return envelopeFrom(envelope.metadata(), objectMapper.convertValue(envelope.payload(), clazz));
    }
}