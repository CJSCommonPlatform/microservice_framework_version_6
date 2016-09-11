package uk.gov.justice.services.core.dispatcher;


import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

public class DispatcherDelegate implements Requester, Sender {
    private final Dispatcher dispatcher;
    private final SystemUserUtil systemUserUtil;

    public DispatcherDelegate(final Dispatcher dispatcher, final SystemUserUtil systemUserUtil) {
        this.dispatcher = dispatcher;
        this.systemUserUtil = systemUserUtil;
    }

    @Override
    public JsonEnvelope request(final JsonEnvelope envelope) {
        return dispatcher.dispatch(envelope);
    }

    @Override
    public JsonEnvelope requestAsAdmin(final JsonEnvelope envelope) {
        return dispatchAsAdmin(envelope);
    }

    @Override
    public void send(final JsonEnvelope envelope) {
        dispatcher.dispatch(envelope);
    }

    @Override
    public void sendAsAdmin(final JsonEnvelope envelope) {
        dispatchAsAdmin(envelope);
    }

    private JsonEnvelope dispatchAsAdmin(final JsonEnvelope envelope) {
        return dispatcher.dispatch(systemUserUtil.asEnvelopeWithSystemUserId(envelope));
    }
}