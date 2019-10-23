package uk.gov.justice.services.jmx.api.command;

public class DisablePublishingCommand extends BaseSystemCommand implements PublishingCommand {

    public static final String DISABLE_PUBLISHING = "DISABLE_PUBLISHING";
    public static final String DESCRIPTION = "Disables the publishing of any newly received events";

    public DisablePublishingCommand() {
        super(DISABLE_PUBLISHING, DESCRIPTION);
    }
}
