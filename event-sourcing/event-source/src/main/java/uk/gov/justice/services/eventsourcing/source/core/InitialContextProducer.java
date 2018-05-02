package uk.gov.justice.services.eventsourcing.source.core;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@ApplicationScoped
public class InitialContextProducer {

  private InitialContext initialContext;

    public synchronized InitialContext getInitialContext() throws NamingException {
        if (initialContext == null) {
            initialContext = new InitialContext();
        }
        return initialContext;
    }
}
