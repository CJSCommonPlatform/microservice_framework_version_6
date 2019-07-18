package uk.gov.justice.services.jmx.api.command;

public class UnshutterSystemCommand extends BaseSystemCommand {

    public static final String UNSHUTTER = "UNSHUTTER";
    private static final String DESCRIPTION = "Unshutters the application.";

    public UnshutterSystemCommand() {
        super(UNSHUTTER, DESCRIPTION);
    }
}
