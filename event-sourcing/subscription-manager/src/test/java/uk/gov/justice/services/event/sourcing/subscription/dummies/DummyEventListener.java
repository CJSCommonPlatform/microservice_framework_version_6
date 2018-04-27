package uk.gov.justice.services.event.sourcing.subscription.dummies;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.annotation.SubscriptionName;

@SuppressWarnings("unused")
@ServiceComponent(EVENT_LISTENER)
@SubscriptionName("my-subscription")
public class DummyEventListener {


    @Handles("my-context.something-happened")
    public void somethingHappened(final JsonEnvelope event) {

    }
}
