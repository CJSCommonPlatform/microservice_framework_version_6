package uk.gov.justice.services.example;

import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.messaging.Envelope;

public class DummyDispatcher implements AsynchronousDispatcher {

    @Override
    public void dispatch(Envelope envelope) {
        // do nothing
    }
}
