package uk.gov.justice.services.jmx.api.command;

public class RemoveTriggerCommand extends BaseSystemCommand {

    public static final String REMOVE_TRIGGER = "REMOVE_TRIGGER";
    private static final String DESCRIPTION = "Removes the 'queue_publish_event' trigger from the event log table so that new events inserted into the event_log table will no longer trigger publishing.";

    public RemoveTriggerCommand() {
        super(REMOVE_TRIGGER, DESCRIPTION);
    }
}
