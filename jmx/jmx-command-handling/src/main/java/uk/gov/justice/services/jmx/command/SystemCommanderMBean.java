package uk.gov.justice.services.jmx.command;

import java.util.List;

import javax.management.MXBean;

@MXBean
public interface SystemCommanderMBean {

    void call(final SystemCommand systemCommand);
    List<SystemCommand> listCommands();
}
