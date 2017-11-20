package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.handler.HandlerMethod;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonValue;

/**
 *
 * Reduces complexity in dispatcher in dealing with different envelope types and envelope
 * payload types.
 *
 * Handles translation between JsonEnvelopes and Envelopes of T in HandlerMethods.
 *
 *      return MethodInvoker
 *              .createMethodInvoker(typeConverter, jsonEnvelopeRepacker)
 *              .invoke(handlerMethod, envelope);
 */
public class MethodInvoker {

    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;
    private JsonEnvelopeRepacker envelopeRepacker;

    private MethodInvoker() {
    }

    private MethodInvoker(EnvelopePayloadTypeConverter envelopePayloadTypeConverter, JsonEnvelopeRepacker envelopeRepacker) {
        this.envelopePayloadTypeConverter = envelopePayloadTypeConverter;
        this.envelopeRepacker = envelopeRepacker;
    }

    public static MethodInvoker createMethodInvoker(final EnvelopePayloadTypeConverter envelopePayloadTypeConverter,
                                                    final JsonEnvelopeRepacker envelopeRepacker) {
        return new MethodInvoker(envelopePayloadTypeConverter, envelopeRepacker);
    }

    public JsonEnvelope invoke(final HandlerMethod handlerMethod, final JsonEnvelope inputEnvelope) {

        final Class<?> payloadClass = handlerMethod.getPayloadType();

        return toJsonEnvelope(
                toJsonValueEnvelope(
                        handlerMethod.execute(
                                toTargetEnvelopeType(inputEnvelope, payloadClass))));
    }

    private Envelope<?> toTargetEnvelopeType(final JsonEnvelope envelope, final Class<?> payloadClass) {
        return envelopePayloadTypeConverter.convert(envelope, payloadClass);
    }

    private JsonEnvelope toJsonEnvelope(final Envelope<JsonValue> envelope) {
        return envelopeRepacker.repack(envelope);
    }

    private Envelope<JsonValue> toJsonValueEnvelope(final Envelope<?> envelope) {
        return envelopePayloadTypeConverter.convert(envelope, JsonValue.class);
    }
}