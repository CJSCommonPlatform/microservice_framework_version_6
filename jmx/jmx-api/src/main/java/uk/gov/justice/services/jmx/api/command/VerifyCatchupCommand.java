package uk.gov.justice.services.jmx.api.command;

public class VerifyCatchupCommand extends BaseSystemCommand {

    public static final String VERIFY_CATCHUP = "VERIFY_CATCHUP";
    private static final String DESCRIPTION = "Runs various verifications on event_log, published_event, processed_event and stream_buffer to check that catchup has run successfully";

    public VerifyCatchupCommand() {
        super(VERIFY_CATCHUP, DESCRIPTION);
    }
}
