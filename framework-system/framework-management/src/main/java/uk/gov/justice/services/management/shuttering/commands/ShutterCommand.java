package uk.gov.justice.services.management.shuttering.commands;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;

public class ShutterCommand extends BaseSystemCommand implements ApplicationShutteringCommand {

    public static final String SHUTTER = "SHUTTER";
    private static final String DESCRIPTION = "Shutters the application to allow for maintenance.";

    public ShutterCommand() {
        super(SHUTTER, DESCRIPTION);
    }
}
