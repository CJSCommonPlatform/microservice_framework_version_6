package uk.gov.justice.services.example;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.messaging.Envelope;

public class DummyDispatcher implements Dispatcher {

    @Override
    public void dispatch(Envelope envelope) {
        // do nothing
    }
}
