package uk.gov.justice.services.jmx.state.observers;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_RECEIVED;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.domain.CommandState;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.state.persistence.SystemCommandStatusRepository;

import java.util.Optional;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Stateless
public class SystemCommandStateBean {

    @Inject
    private SystemCommandStatusRepository systemCommandStatusRepository;

    @Transactional(REQUIRES_NEW)
    public void addSystemCommandState(final SystemCommandStatus systemCommandStatus) {
        systemCommandStatusRepository.add(systemCommandStatus);
    }

    @Transactional(REQUIRES_NEW)
    public Optional<SystemCommandStatus> getCommandStatus(final UUID commandId) {
        return systemCommandStatusRepository.findLatestStatusById(commandId);
    }

    @Transactional(REQUIRES_NEW)
    public boolean commandInProgress(final SystemCommand systemCommand) {
        final Optional<SystemCommandStatus> latestStatusByType = systemCommandStatusRepository.findLatestStatusByType(systemCommand);

        if (latestStatusByType.isPresent()) {
            final CommandState commandState = latestStatusByType.get().getCommandState();

            return commandState == COMMAND_RECEIVED || commandState == COMMAND_IN_PROGRESS;
        }

        return false;
    }
}
