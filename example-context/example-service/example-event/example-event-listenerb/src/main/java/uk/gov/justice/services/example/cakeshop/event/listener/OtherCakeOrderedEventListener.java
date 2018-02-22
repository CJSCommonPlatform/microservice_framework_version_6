package uk.gov.justice.services.example.cakeshop.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.example.cakeshop.persistence.entity.CakeOrder;
import uk.gov.justice.services.messaging.Envelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class OtherCakeOrderedEventListener {

    Logger logger = LoggerFactory.getLogger(OtherCakeOrderedEventListener.class);

    @Handles("other.cake-ordered")
    public void handle(final Envelope<CakeOrder> envelope) {
        logger.info("I received an {} event", envelope.metadata().name());
    }
}