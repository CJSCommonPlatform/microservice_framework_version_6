package uk.gov.justice.services.jdbc.persistence;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class InitialContextFactory {

    public InitialContext create() throws NamingException {
        return new InitialContext();
    }
}
