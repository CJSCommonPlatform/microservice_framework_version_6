package uk.gov.justice.services.jdbc.persistence;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@ApplicationScoped
public class InitialContextProvider {

    private InitialContext initialContext;

    public synchronized InitialContext getInitialContext() throws NamingException {

        if (initialContext == null) {
            initialContext = new InitialContext();
        }

        return initialContext;
    }
}
