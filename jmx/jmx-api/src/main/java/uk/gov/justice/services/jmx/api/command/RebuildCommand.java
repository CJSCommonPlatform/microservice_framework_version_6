package uk.gov.justice.services.jmx.api.command;

public class RebuildCommand extends BaseSystemCommand {

    public static final String REBUILD = "REBUILD";
    private static final String DESCRIPTION = "Rebuilds PublishedEvents and renumbers the Events whilst shuttered";

    public RebuildCommand() {
        super(REBUILD, DESCRIPTION);
    }
}
