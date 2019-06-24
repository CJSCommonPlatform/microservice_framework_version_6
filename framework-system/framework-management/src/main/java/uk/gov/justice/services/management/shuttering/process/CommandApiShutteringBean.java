package uk.gov.justice.services.management.shuttering.process;

import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.messaging.jms.EnvelopeSenderSelector;
import uk.gov.justice.services.shuttering.domain.ShutteredCommand;
import uk.gov.justice.services.shuttering.persistence.ShutteringRepository;

import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Stateless
public class CommandApiShutteringBean {

    @Inject
    private EnvelopeSenderSelector envelopeSenderSelector;

    @Inject
    private ShutteringRepository shutteringRepository;

    @Inject
    private ShutteredCommandSender shutteredCommandSender;

    public void shutter() {
        envelopeSenderSelector.setShuttered(true);
    }

    @Transactional(REQUIRED)
    public void unshutter() {

        try (final Stream<ShutteredCommand> shutteredCommandStream = shutteringRepository.streamShutteredCommands()) {
            shutteredCommandStream.forEach(shutteredCommandSender::sendAndDelete);
        }

        envelopeSenderSelector.setShuttered(false);
    }
}
