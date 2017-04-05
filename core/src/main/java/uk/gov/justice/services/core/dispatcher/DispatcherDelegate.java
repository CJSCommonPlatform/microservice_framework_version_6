package uk.gov.justice.services.core.dispatcher;


import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

public class DispatcherDelegate implements Requester, Sender {
    private final Dispatcher dispatcher;
    private final SystemUserUtil systemUserUtil;
    private final EnvelopeValidator envelopeValidator;

    public DispatcherDelegate(final Dispatcher dispatcher,
                              final SystemUserUtil systemUserUtil,
                              final EnvelopeValidator envelopeValidator) {
        this.dispatcher = dispatcher;
        this.systemUserUtil = systemUserUtil;
        this.envelopeValidator = envelopeValidator;
    }

    @Override
    public JsonEnvelope request(final JsonEnvelope envelope) {
        final JsonEnvelope response = dispatcher.dispatch(envelope);
        envelopeValidator.validate(response);
        return response;
    }

    @Override
    public JsonEnvelope requestAsAdmin(final JsonEnvelope envelope) {
        final JsonEnvelope response = dispatchAsAdmin(envelope);
        envelopeValidator.validate(response);
        return response;
    }

    @Override
    public void send(final JsonEnvelope envelope) {
        envelopeValidator.validate(envelope);
        dispatcher.dispatch(envelope);
    }

    @Override
    public void sendAsAdmin(final JsonEnvelope envelope) {
        envelopeValidator.validate(envelope);
        dispatchAsAdmin(envelope);
    }

    private JsonEnvelope dispatchAsAdmin(final JsonEnvelope envelope) {
        return dispatcher.dispatch(systemUserUtil.asEnvelopeWithSystemUserId(envelope));
    }
}
