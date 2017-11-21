package uk.gov.justice.services.core.dispatcher;


import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonValue;

public class DispatcherDelegate implements Requester, Sender {

    private final Dispatcher dispatcher;
    private final SystemUserUtil systemUserUtil;
    private final RequestResponseEnvelopeValidator requestResponseEnvelopeValidator;
    private final EnvelopePayloadTypeConverter envelopePayloadTypeConverter;
    private final JsonEnvelopeRepacker jsonEnvelopeRepacker;

    public DispatcherDelegate(final Dispatcher dispatcher,
                              final SystemUserUtil systemUserUtil,
                              final RequestResponseEnvelopeValidator requestResponseEnvelopeValidator,
                              final EnvelopePayloadTypeConverter envelopePayloadTypeConverter,
                              final JsonEnvelopeRepacker jsonEnvelopeRepacker) {
        this.dispatcher = dispatcher;
        this.systemUserUtil = systemUserUtil;
        this.requestResponseEnvelopeValidator = requestResponseEnvelopeValidator;
        this.envelopePayloadTypeConverter = envelopePayloadTypeConverter;
        this.jsonEnvelopeRepacker = jsonEnvelopeRepacker;
    }

    @Override
    public JsonEnvelope request(final Envelope<?> envelope) {
        final Envelope<JsonValue> jsonValueEnvelope = envelopePayloadTypeConverter.convert(envelope, JsonValue.class);
        final JsonEnvelope jsonEnvelope = jsonEnvelopeRepacker.repack(jsonValueEnvelope);
        final JsonEnvelope response = dispatcher.dispatch(jsonEnvelope);

        requestResponseEnvelopeValidator.validateResponse(response);

        return response;
    }

    @Override
    public <T> Envelope<T> request(final Envelope<?> envelope, final Class<T> clazz) {
        final Envelope<JsonValue> jsonValueEnvelope = envelopePayloadTypeConverter.convert(envelope, JsonValue.class);
        final JsonEnvelope jsonEnvelope = jsonEnvelopeRepacker.repack(jsonValueEnvelope);
        final JsonEnvelope response = dispatcher.dispatch(jsonEnvelope);

        requestResponseEnvelopeValidator.validateResponse(response);

        return envelopePayloadTypeConverter.convert(response, clazz);
    }

    @Override
    public JsonEnvelope requestAsAdmin(final JsonEnvelope envelope) {
        final JsonEnvelope response = dispatchAsAdmin(envelope);
        requestResponseEnvelopeValidator.validateResponse(response);
        return response;
    }

    @Override
    public void send(final Envelope<?> envelope) {
        final Envelope<JsonValue> jsonValueEnvelope = envelopePayloadTypeConverter.convert(envelope, JsonValue.class);
        final JsonEnvelope jsonEnvelope = jsonEnvelopeRepacker.repack(jsonValueEnvelope);

        requestResponseEnvelopeValidator.validateRequest(jsonEnvelope);

        dispatcher.dispatch(jsonEnvelope);
    }

    @Override
    public void sendAsAdmin(final JsonEnvelope envelope) {
        requestResponseEnvelopeValidator.validateRequest(envelope);
        dispatchAsAdmin(envelope);
    }

    private JsonEnvelope dispatchAsAdmin(final JsonEnvelope envelope) {
        return dispatcher.dispatch(systemUserUtil.asEnvelopeWithSystemUserId(envelope));
    }
}
