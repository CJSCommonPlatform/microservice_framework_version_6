package uk.gov.justice.services.management.ping.commands;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;

public class PingCommand extends BaseSystemCommand {

    public static final String PING = "PING";
    public static final String DESCRIPTION = "Outputs a PING log message. Use for testing connections.";

    public PingCommand() {
        super(PING, DESCRIPTION);
    }
}
