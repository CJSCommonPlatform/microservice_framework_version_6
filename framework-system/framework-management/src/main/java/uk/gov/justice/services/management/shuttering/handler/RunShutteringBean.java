package uk.gov.justice.services.management.shuttering.handler;

import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLogger;
import uk.gov.justice.services.management.shuttering.process.ShutteringProcessRunner;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Stateless
public class RunShutteringBean {

    @Inject
    private MdcLogger mdcLogger;

    @Inject
    private ShutteringProcessRunner shutteringProcessRunner;

    @Transactional(REQUIRED)
    public void runShuttering(final UUID commandId, final SystemCommand systemCommand) {
        mdcLogger.mdcLoggerConsumer().accept(() -> shutteringProcessRunner.runShuttering(commandId, systemCommand));
    }
}
