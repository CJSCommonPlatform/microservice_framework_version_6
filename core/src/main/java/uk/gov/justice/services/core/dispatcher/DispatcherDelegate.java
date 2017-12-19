package uk.gov.justice.services.core.dispatcher;


import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

public class DispatcherDelegate implements Requester, Sender {

    private final Dispatcher dispatcher;
    private final SystemUserUtil systemUserUtil;
    private final RequestResponseEnvelopeValidator requestResponseEnvelopeValidator;

    public DispatcherDelegate(final Dispatcher dispatcher,
                              final SystemUserUtil systemUserUtil,
                              final RequestResponseEnvelopeValidator requestResponseEnvelopeValidator) {
        this.dispatcher = dispatcher;
        this.systemUserUtil = systemUserUtil;
        this.requestResponseEnvelopeValidator = requestResponseEnvelopeValidator;
    }

    @Override
    public JsonEnvelope request(final JsonEnvelope envelope) {
        final JsonEnvelope response = dispatcher.dispatch(envelope);
        requestResponseEnvelopeValidator.validateResponse(response);
        return response;
    }

    @Override
    public JsonEnvelope requestAsAdmin(final JsonEnvelope envelope) {
        final JsonEnvelope response = dispatchAsAdmin(envelope);
        requestResponseEnvelopeValidator.validateResponse(response);
        return response;
    }

    @Override
    public void send(final JsonEnvelope envelope) {
        requestResponseEnvelopeValidator.validateRequest(envelope);
        dispatcher.dispatch(envelope);
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
