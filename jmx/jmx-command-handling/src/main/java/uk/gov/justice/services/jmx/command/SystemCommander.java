package uk.gov.justice.services.jmx.command;

import static java.lang.String.format;

import javax.inject.Inject;

import org.slf4j.Logger;

public class SystemCommander implements SystemCommanderMBean {

    @Inject
    private Logger logger;

    @Inject
    private SystemCommandStore systemCommandStore;

    @Override
    public void call(final SystemCommand systemCommand) {

        logger.info(format("Received System Command '%s'", systemCommand.getName()));

        systemCommandStore.findCommandProxy(systemCommand).invokeCommand();
    }
}
