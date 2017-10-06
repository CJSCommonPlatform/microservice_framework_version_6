package uk.gov.justice.services.example.cakeshop.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.example.cakeshop.persistence.CakeOrderRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.CakeOrder;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class CakeOrderedEventListener {

    Logger logger = LoggerFactory.getLogger(CakeOrderedEventListener.class);

    @Inject
    CakeOrderRepository repository;

    @Inject
    JsonObjectToObjectConverter converter;

    @Handles("example.events.cake-ordered")
    public void handle(final JsonEnvelope envelope) {
        logger.info("=============> Inside cake-ordered Event Listener: " + envelope);
        final CakeOrder cakeOrder = converter.convert(envelope.payloadAsJsonObject(), CakeOrder.class);
        repository.save(cakeOrder);
    }
}