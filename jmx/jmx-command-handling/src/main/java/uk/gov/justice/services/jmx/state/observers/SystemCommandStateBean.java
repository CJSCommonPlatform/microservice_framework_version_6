package uk.gov.justice.services.jmx.state.observers;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

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
        return systemCommandStatusRepository.findLatestStatus(commandId);
    }
}
