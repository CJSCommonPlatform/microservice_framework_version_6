package uk.gov.justice.services.example.cakeshop.command.handler;


import uk.gov.justice.services.example.cakeshop.domain.event.CakeOrdered;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class EventFactory {

    @Inject
    ObjectMapper objectMapper;

    public CakeOrdered cakeOrderedEventFrom(final JsonEnvelope command) {
        try {
            return objectMapper.readValue(command.payload().toString(), CakeOrdered.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
