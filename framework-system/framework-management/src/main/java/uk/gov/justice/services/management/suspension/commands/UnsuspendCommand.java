package uk.gov.justice.services.management.suspension.commands;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;

public class UnsuspendCommand extends BaseSystemCommand implements SuspensionCommand {

    public static final String UNSUSPEND = "UNSUSPEND";
    private static final String DESCRIPTION = "Unsuspends the application.";

    public UnsuspendCommand() {
        super(UNSUSPEND, DESCRIPTION);
    }
}
