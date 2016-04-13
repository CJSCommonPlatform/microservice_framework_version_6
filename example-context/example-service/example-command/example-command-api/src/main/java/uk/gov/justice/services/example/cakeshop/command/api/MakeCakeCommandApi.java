package uk.gov.justice.services.example.cakeshop.command.api;

import org.slf4j.Logger;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

@ServiceComponent(COMMAND_API)
public class MakeCakeCommandApi {

    private static final Logger LOGGER = getLogger(MakeCakeCommandApi.class);

    @Inject
    Sender sender;

    @Handles("cakeshop.command.make-cake")
    public void handle(final JsonEnvelope envelope) {
        LOGGER.info("=============> Inside make-cake Command API");

        sender.send(envelope);
    }
}
