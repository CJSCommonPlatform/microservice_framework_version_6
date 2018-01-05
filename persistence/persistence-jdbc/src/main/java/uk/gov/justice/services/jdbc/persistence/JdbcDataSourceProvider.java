package uk.gov.justice.services.jdbc.persistence;

import static java.lang.String.format;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ApplicationScoped
public class JdbcDataSourceProvider {

    private static final String JNDI_DS_EVENT_STORE_PATTERN = "java:/app/%s/DS.eventstore";

    @Resource(lookup = "java:app/AppName")
    private String warFileName;

    private Context initialContext;
    private DataSource datasource = null;


    public JdbcDataSourceProvider() throws NamingException {
        initialContext = getInitialContext();
    }

    public Context getInitialContext() throws NamingException {
        if (initialContext == null) {
            initialContext = new InitialContext();
        }
        return initialContext;
    }

    public DataSource getDataSource() {
        if (datasource == null) {
            try {
                datasource = (DataSource) getInitialContext().lookup(jndiName());
            } catch (final NamingException e) {
                throw new JdbcRepositoryException(e);
            }
        }
        return datasource;
    }

    public String jndiName() {
        return format(JNDI_DS_EVENT_STORE_PATTERN, warFileName);
    }
}
