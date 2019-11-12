package uk.gov.justice.services.management.suspension.process;

import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

public class SuspendablesRunner {

    @Inject
    private SuspensionRunner suspensionRunner;

    @Inject
    private UnsuspensionRunner unsuspensionRunner;


    public List<SuspensionResult> findAndRunSuspendables(final UUID commandId, final SuspensionCommand suspensionCommand) {

        if (suspensionCommand.isSuspendCommand()) {
            return suspensionRunner.runSuspension(commandId, suspensionCommand);
        }

        return unsuspensionRunner.runUsuspension(commandId, suspensionCommand);
    }
}
