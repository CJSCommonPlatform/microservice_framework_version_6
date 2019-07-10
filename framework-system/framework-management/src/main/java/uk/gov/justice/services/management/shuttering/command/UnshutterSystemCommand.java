package uk.gov.justice.services.management.shuttering.command;

import uk.gov.justice.services.jmx.command.BaseSystemCommand;

public class UnshutterSystemCommand extends BaseSystemCommand {

    public static final String UNSHUTTER_APPLICATION = "UNSHUTTER_APPLICATION";
    private static final String DESCRIPTION = "Unshutters the application.";

    public UnshutterSystemCommand() {
        super(UNSHUTTER_APPLICATION, DESCRIPTION);
    }
}
