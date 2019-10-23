package uk.gov.justice.services.jmx.api.command;

public class EnablePublishingCommand extends BaseSystemCommand implements PublishingCommand {

    public static final String ENABLE_PUBLISHING = "ENABLE_PUBLISHING";
    public static final String DESCRIPTION = "Enables the publishing of any newly received events";

    public EnablePublishingCommand() {
        super(ENABLE_PUBLISHING, DESCRIPTION);
    }
}
