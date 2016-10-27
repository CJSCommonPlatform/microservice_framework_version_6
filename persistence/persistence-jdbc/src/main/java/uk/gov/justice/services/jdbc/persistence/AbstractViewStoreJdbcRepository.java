package uk.gov.justice.services.jdbc.persistence;


import static java.lang.String.format;

import javax.naming.NamingException;

public abstract class AbstractViewStoreJdbcRepository<T> extends AbstractJdbcRepository<T> {

    private static final String VIEW_STORE_JNDI_PATTERN = "java:/DS.%s";

    @Override
    protected String jndiName() throws NamingException {
        final String warFileName = warFileName();
        final String contextName = warFileName.substring(0, warFileName.indexOf("-"));
        return format(VIEW_STORE_JNDI_PATTERN, contextName);
    }
}
