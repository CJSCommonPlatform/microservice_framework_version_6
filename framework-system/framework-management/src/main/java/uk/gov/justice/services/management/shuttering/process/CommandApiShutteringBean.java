package uk.gov.justice.services.management.shuttering.process;

import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.messaging.jms.EnvelopeSenderSelector;
import uk.gov.justice.services.shuttering.domain.ShutteredCommand;
import uk.gov.justice.services.shuttering.persistence.ShutteringRepository;

import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
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

    @Inject
    private Event<ShutteringCompleteEvent> shutteringCompleteEventFirer;

    @Inject
    private Event<UnshutteringCompleteEvent> unshutteringCompleteEventFirer;

    @Inject
    private UtcClock clock;

    public void shutter(final SystemCommand target) {
        envelopeSenderSelector.setShuttered(true);

        shutteringCompleteEventFirer.fire(new ShutteringCompleteEvent(target, clock.now()));
    }

    @Transactional(REQUIRED)
    public void unshutter(final SystemCommand target) {

        try (final Stream<ShutteredCommand> shutteredCommandStream = shutteringRepository.streamShutteredCommands()) {
            shutteredCommandStream.forEach(shutteredCommandSender::sendAndDelete);
        }

        envelopeSenderSelector.setShuttered(false);

        unshutteringCompleteEventFirer.fire(new UnshutteringCompleteEvent(target, clock.now()));
    }
}
