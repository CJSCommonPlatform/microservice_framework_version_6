package uk.gov.justice.services.messaging.jms;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.shuttering.domain.StoredCommand;
import uk.gov.justice.services.shuttering.persistence.StoredCommandRepository;

import java.util.UUID;

import javax.inject.Inject;

public class ShutteringStoreSender implements EnvelopeSender {

    @Inject
    private StoredCommandRepository storedCommandRepository;

    @Inject
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    private UtcClock clock;

    public void send(final JsonEnvelope command, final String destinationName) {

        final UUID envelopeId = command.metadata().id();

        final String commandJson = jsonObjectEnvelopeConverter.asJsonString(command);

        final StoredCommand storedCommand = new StoredCommand(
                envelopeId,
                commandJson,
                destinationName,
                clock.now());

        storedCommandRepository.save(storedCommand);
    }
}
