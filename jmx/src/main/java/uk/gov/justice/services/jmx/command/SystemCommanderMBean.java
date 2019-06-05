package uk.gov.justice.services.jmx.command;

import javax.management.MXBean;

@MXBean
public interface SystemCommanderMBean {

    void runCommand(final SystemCommand systemCommand);
}
