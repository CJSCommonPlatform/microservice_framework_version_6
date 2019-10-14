package uk.gov.justice.services.management.shuttering.handler;

import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.jmx.api.command.ApplicationShutteringCommand;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;
import uk.gov.justice.services.management.shuttering.process.ShutteringProcessRunner;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.transaction.Transactional;

@Stateless
public class RunShutteringBean {

    @Inject
    private ShutteringProcessRunner shutteringProcessRunner;

    @Interceptors(MdcLoggerInterceptor.class)
    @Transactional(REQUIRED)
    public void runShuttering(final UUID commandId, final ApplicationShutteringCommand applicationShutteringCommand) {
        shutteringProcessRunner.runShuttering(commandId, applicationShutteringCommand);
    }
}
