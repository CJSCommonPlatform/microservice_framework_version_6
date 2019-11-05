package uk.gov.justice.services.management.ping.handler;

import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;
import static uk.gov.justice.services.management.ping.commands.PingCommand.PING;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.ping.commands.PingCommand;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class PingHandler {

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(PING)
    public void ping(@SuppressWarnings("unused") final PingCommand pingCommand, @SuppressWarnings("unused") final UUID commandId) {

        final ZonedDateTime startedAt = clock.now();
        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                pingCommand,
                COMMAND_IN_PROGRESS,
                startedAt,
                "Ping command received"
        ));

        logger.info("********** Received system command '" + PING + "' at " + startedAt + " **********");

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                pingCommand,
                COMMAND_COMPLETE,
                clock.now(),
                "Ping command complete"
        ));
    }
}
