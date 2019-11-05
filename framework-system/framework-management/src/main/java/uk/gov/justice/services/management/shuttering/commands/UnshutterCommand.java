package uk.gov.justice.services.management.shuttering.commands;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;

public class UnshutterCommand extends BaseSystemCommand implements ApplicationShutteringCommand {

    public static final String UNSHUTTER = "UNSHUTTER";
    private static final String DESCRIPTION = "Unshutters the application.";

    public UnshutterCommand() {
        super(UNSHUTTER, DESCRIPTION);
    }
}
