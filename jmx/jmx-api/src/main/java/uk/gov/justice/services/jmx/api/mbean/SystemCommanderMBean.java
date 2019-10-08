package uk.gov.justice.services.jmx.api.mbean;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;

import java.util.List;
import java.util.UUID;

import javax.management.MXBean;

@MXBean
public interface SystemCommanderMBean {

    UUID call(final SystemCommand systemCommand);
    List<SystemCommand> listCommands();
    SystemCommandStatus getCommandStatus(UUID commandId);
}
