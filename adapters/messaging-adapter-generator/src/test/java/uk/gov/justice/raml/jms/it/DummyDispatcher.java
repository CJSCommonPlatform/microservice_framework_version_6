package uk.gov.justice.raml.jms.it;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Singleton;

@Singleton
public class DummyDispatcher implements Dispatcher {

    private Envelope envelope;

    @Override
    public void dispatch(Envelope envelope) {
        synchronized (this) {
            this.envelope = envelope;
        }
    }

    public Envelope receivedEnvelope() {
        synchronized (this) {
            return envelope;
        }
    }

    public void reset() {
        synchronized (this) {
            envelope = null;
        }
    }
}
