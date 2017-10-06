package uk.gov.justice.services.example.cakeshop.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.example.cakeshop.persistence.CakeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Cake;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(value = Component.EVENT_LISTENER)
public class CakeMadeEventListener {

    Logger logger = LoggerFactory.getLogger(CakeMadeEventListener.class);

    @Inject
    CakeRepository cakeRepository;

    @Inject
    JsonObjectToObjectConverter converter;

    @Handles("example.events.cake-made")
    public void handle(final JsonEnvelope envelope) {

        logger.info("=============> Inside cake-made Event Listener");

        final Cake cake = converter.convert(envelope.payloadAsJsonObject(), Cake.class);
        cakeRepository.save(cake);


    }
}
