package uk.gov.justice.raml.jms.it.handler;

import uk.gov.justice.services.messaging.Envelope;

public abstract class BasicHandler {

    protected Envelope receivedEnvelope;

    public void reset() {
        receivedEnvelope = null;
    }

    public Envelope receivedEnvelope() {
        return receivedEnvelope;
    }

}