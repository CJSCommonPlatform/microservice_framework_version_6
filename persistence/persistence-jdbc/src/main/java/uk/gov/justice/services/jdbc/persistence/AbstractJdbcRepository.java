package uk.gov.justice.services.jdbc.persistence;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public abstract class AbstractJdbcRepository {

    @Resource(lookup = "java:app/AppName")
    String warFileName;

    Context initialContext;
    DataSource datasource;

    protected Context getInitialContext() throws NamingException {
        if (initialContext == null) {
            initialContext = new InitialContext();
        }
        return initialContext;
    }

    protected DataSource getDataSource() throws NamingException {
        if (datasource == null) {
            datasource = (DataSource) getInitialContext().lookup(jndiName());
        }
        return datasource;
    }

    protected String warFileName() throws NamingException {
        return warFileName;
    }

    protected abstract String jndiName() throws NamingException;

}
