package uk.gov.justice.services.example.cakeshop.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Cake;
import uk.gov.justice.services.messaging.Envelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class OtherCakeMadeEventListener {

    Logger logger = LoggerFactory.getLogger(OtherCakeMadeEventListener.class);

    @Handles("other.cake-made")
    public void handle(final Envelope<Cake> envelope) {
        logger.info("I received an {} event", envelope.metadata().name());
    }
}
