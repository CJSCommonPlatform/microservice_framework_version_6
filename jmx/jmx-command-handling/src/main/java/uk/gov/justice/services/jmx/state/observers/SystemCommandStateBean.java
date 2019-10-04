package uk.gov.justice.services.jmx.state.observers;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.jmx.state.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.state.persistence.SystemCommandStatusRepository;

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
}
