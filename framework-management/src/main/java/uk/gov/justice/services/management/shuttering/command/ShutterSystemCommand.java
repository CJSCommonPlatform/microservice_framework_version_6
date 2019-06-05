package uk.gov.justice.services.management.shuttering.command;

import uk.gov.justice.services.jmx.command.BaseSystemCommand;

public class ShutterSystemCommand extends BaseSystemCommand {

    public static final String SHUTTER_APPLICATION = "SHUTTER_APPLICATION";

    public ShutterSystemCommand() {
        super(SHUTTER_APPLICATION);
    }
}
