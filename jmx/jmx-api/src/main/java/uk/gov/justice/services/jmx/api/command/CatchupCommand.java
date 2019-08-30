package uk.gov.justice.services.jmx.api.command;

public class CatchupCommand extends BaseSystemCommand {

    public static final String CATCHUP = "CATCHUP";
    private static final String DESCRIPTION = "Catches up and republishes all Events since the last known event";

    public CatchupCommand() {
        super(CATCHUP, DESCRIPTION);
    }
}
