package uk.gov.justice.services.example;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

/**
 * Created by david on 07/03/16.
 */
@ServiceComponent(COMMAND_API)
public class DummyHandler {

    @Handles("people.commands.create-user")
    public void create(final Envelope envelope) {

    }
    @Handles("people.commands.update-user")
    public void update(final Envelope envelope) {

    }
}
