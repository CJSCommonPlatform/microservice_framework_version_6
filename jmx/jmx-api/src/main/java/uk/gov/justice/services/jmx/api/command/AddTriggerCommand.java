package uk.gov.justice.services.jmx.api.command;

public class AddTriggerCommand extends BaseSystemCommand {

    public static final String ADD_TRIGGER = "ADD_TRIGGER";
    private static final String DESCRIPTION = "Adds the 'queue_publish_event' trigger to the event log table so that new events inserted into the event_log table will trigger publishing.";

    public AddTriggerCommand() {
        super(ADD_TRIGGER, DESCRIPTION);
    }
}
