package uk.gov.justice.services.jdbc.persistence;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Created by justin on 02/08/2016.
 */
public class AbstractJdbcRepository {
    static final String JNDI_APP_NAME_LOOKUP = "java:app/AppName";
    private final String jndiDataSourceName;

    Context initialContext;
    DataSource datasource;


    public AbstractJdbcRepository(String jndiDataSourceName) {
        this.jndiDataSourceName = jndiDataSourceName;
    }

    private Context getInitialContext() throws NamingException {
        if (initialContext == null) {
            initialContext = new InitialContext();
        }

        return initialContext;
    }

    protected DataSource getDataSource() throws NamingException {
        if (datasource == null) {
            final String appName = (String) getInitialContext().lookup("java:app/AppName");

            datasource = (DataSource) getInitialContext().lookup(String.format("java:/app/%s/DS.%s", appName, jndiDataSourceName));
        }

        return datasource;
    }
}
