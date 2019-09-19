package uk.gov.justice.services.jmx.api.command;

public class ValidateCatchupCommand extends BaseSystemCommand {

    public static final String VALIDATE_CATCHUP = "VALIDATE_CATCHUP";
    private static final String DESCRIPTION = "Validates that all events are present and numbered correctly in the published_event and processed_event tables";

    public ValidateCatchupCommand() {
        super(VALIDATE_CATCHUP, DESCRIPTION);
    }
}
