package uk.gov.justice.services.management.shuttering.lifecycle;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsSender;
import uk.gov.justice.services.shuttering.domain.ShutteredCommand;
import uk.gov.justice.services.shuttering.persistence.ShutteringRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class ShutteredCommandSender {

    @Inject
    private ShutteringRepository shutteringRepository;

    @Inject
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    private JmsSender jmsSender;

    @Transactional(REQUIRES_NEW)
    public void sendAndDelete(final ShutteredCommand shutteredCommand) {

        final String commandJsonEnvelope = shutteredCommand.getCommandJsonEnvelope();
        final JsonEnvelope jsonEnvelope = jsonObjectEnvelopeConverter.asEnvelope(commandJsonEnvelope);
        final String destination = shutteredCommand.getDestination();

        jmsSender.send(jsonEnvelope, destination);

        shutteringRepository.delete(shutteredCommand.getEnvelopeId());
    }
}
