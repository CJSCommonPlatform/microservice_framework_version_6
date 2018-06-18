package uk.gov.justice.services.test.utils.persistence;

import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;

import javax.naming.Context;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@Deprecated
public class AbstractJdbcRepositoryIT<T extends AbstractJdbcRepository> {
    protected DataSource dataSource;
    protected T jdbcRepository;
    private final String liquibaseLocation;

    public AbstractJdbcRepositoryIT(final String liquibaseLocation) {

        this.liquibaseLocation = liquibaseLocation;
    }

    protected void registerDataSource() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES,
                "org.apache.naming");

        dataSource = new TestEventStoreDataSourceFactory(liquibaseLocation).createDataSource();

        setField(jdbcRepository, "datasource", dataSource);
        initDatabase();
    }

    private void initDatabase() throws Exception {
        Liquibase liquibase = new Liquibase(liquibaseLocation,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
    }
}
