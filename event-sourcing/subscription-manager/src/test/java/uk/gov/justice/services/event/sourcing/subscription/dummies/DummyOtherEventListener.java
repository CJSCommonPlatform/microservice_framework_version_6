package uk.gov.justice.services.event.sourcing.subscription.dummies;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

@SuppressWarnings("unused")
@ServiceComponent(EVENT_LISTENER)
public class DummyOtherEventListener {

    @Handles("my-context.something-else-happened")
    public void somethingHappened(final JsonEnvelope event) {

    }
}
