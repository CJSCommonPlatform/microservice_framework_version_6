package uk.gov.justice.services.jmx.api.command;

public class ValidatePublishedEventsCommand extends BaseSystemCommand {

    public static final String VALIDATE_EVENTS = "VALIDATE_EVENTS";
    public static final String DESCRIPTION = "Validates that all payloads of published events abide by their schemas.";

    public ValidatePublishedEventsCommand() {
        super(VALIDATE_EVENTS, DESCRIPTION);
    }
}
