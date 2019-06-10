package uk.gov.justice.services.management.shuttering.command;

import uk.gov.justice.services.jmx.command.BaseSystemCommand;

public class UnshutterSystemCommand extends BaseSystemCommand {

    public static final String UNSHUTTER_APPLICATION = "UNSHUTTER_APPLICATION";

    public UnshutterSystemCommand() {
        super(UNSHUTTER_APPLICATION);
    }
}
