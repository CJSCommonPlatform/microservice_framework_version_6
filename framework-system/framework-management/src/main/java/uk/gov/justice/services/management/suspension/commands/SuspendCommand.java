package uk.gov.justice.services.management.suspension.commands;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;

public class SuspendCommand extends BaseSystemCommand implements SuspensionCommand {

    public static final String SUSPEND = "SUSPEND";
    private static final String DESCRIPTION = "Suspends the application to allow for maintenance.";

    public SuspendCommand() {
        super(SUSPEND, DESCRIPTION);
    }
}
