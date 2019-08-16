package uk.gov.justice.services.jmx.api.mbean;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.state.ApplicationManagementState;

import java.util.List;

import javax.management.MXBean;

@MXBean
public interface SystemCommanderMBean {

    void call(final SystemCommand systemCommand);
    List<SystemCommand> listCommands();
    ApplicationManagementState getApplicationState();
}
