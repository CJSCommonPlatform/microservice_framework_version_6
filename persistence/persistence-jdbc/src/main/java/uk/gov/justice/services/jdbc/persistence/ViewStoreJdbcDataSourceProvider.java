package uk.gov.justice.services.jdbc.persistence;

import static java.lang.String.format;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ViewStoreJdbcDataSourceProvider {

    private static final String VIEW_STORE_JNDI_PATTERN = "java:/DS.%s";

    @Resource(lookup = "java:app/AppName")
    private String warFileName;

    private Context initialContext;

    public ViewStoreJdbcDataSourceProvider() {}

    ViewStoreJdbcDataSourceProvider(final String warFileName, final Context initialContext) {
        this.warFileName = warFileName;
        this.initialContext = initialContext;
    }

    private DataSource datasource = null;

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

    private String jndiName() {

        return jndiName(warFileName);
    }

    public String jndiName(final String warFileName) {

        final String contextName;

        if (warFileName.contains("-")) {
            contextName = warFileName.substring(0, warFileName.indexOf('-'));
        }
        else {
            contextName = warFileName;
        }

        return format(VIEW_STORE_JNDI_PATTERN, contextName);
    }
}

