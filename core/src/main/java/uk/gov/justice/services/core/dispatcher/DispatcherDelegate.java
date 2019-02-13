package uk.gov.justice.services.core.dispatcher;


import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.function.Function;

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
        return dispatchAndValidateResponse(envelope);
    }

    @Override
    public <T> Envelope<T> request(final Envelope<?> envelope, final Class<T> clazz) {
        final JsonEnvelope response = dispatchAndValidateResponse(envelope);

        return envelopePayloadTypeConverter.convert(response, clazz);
    }

    @Override
    public JsonEnvelope requestAsAdmin(final JsonEnvelope envelope) {
        final JsonEnvelope response = dispatchAsAdmin().apply(envelope);
        requestResponseEnvelopeValidator.validateResponse(response);
        return response;
    }

    @Override
    public <T> Envelope<T> requestAsAdmin(final Envelope<?> envelope, final Class<T> clazz) {
        final JsonEnvelope response = dispatchAsAdmin().compose(convertAndRepackEnvelope()).apply(envelope);

        requestResponseEnvelopeValidator.validateResponse(response);

        return envelopePayloadTypeConverter.convert(response, clazz);
    }

    @Override
    public void send(final Envelope<?> envelope) {
        final JsonEnvelope jsonEnvelope = convertAndRepackEnvelope().apply(envelope);

        requestResponseEnvelopeValidator.validateRequest(jsonEnvelope);

        dispatch().apply(jsonEnvelope);
    }

    @Override
    public void sendAsAdmin(final JsonEnvelope envelope) {
        requestResponseEnvelopeValidator.validateRequest(envelope);
        dispatchAsAdmin().apply(envelope);
    }

    @Override
    public void sendAsAdmin(final Envelope<?> envelope) {
        final JsonEnvelope jsonEnvelope = convertAndRepackEnvelope().apply(envelope);

        requestResponseEnvelopeValidator.validateRequest(jsonEnvelope);

        dispatchAsAdmin().apply(jsonEnvelope);
    }

    private JsonEnvelope dispatchAndValidateResponse(final Envelope<?> envelope) {
        final JsonEnvelope response = dispatch().compose(convertAndRepackEnvelope()).apply(envelope);

        requestResponseEnvelopeValidator.validateResponse(response);
        return response;
    }

    private Function<Envelope<?>, JsonEnvelope> convertAndRepackEnvelope() {
        return (jsonEnvelope) -> jsonEnvelopeRepacker.repack(
                envelopePayloadTypeConverter.convert(jsonEnvelope, JsonValue.class));
    }

    private Function<JsonEnvelope, JsonEnvelope> dispatch() {
        return (jsonEnvelope) -> dispatcher.dispatch(jsonEnvelope);
    }

    private Function<JsonEnvelope, JsonEnvelope> dispatchAsAdmin(){
        return (jsonEnvelope) -> dispatcher.dispatch(systemUserUtil.asEnvelopeWithSystemUserId(jsonEnvelope));
    }
}
