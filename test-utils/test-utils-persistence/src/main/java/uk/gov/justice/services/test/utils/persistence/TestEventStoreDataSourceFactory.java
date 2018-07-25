package uk.gov.justice.services.test.utils.persistence;

import static java.lang.String.format;

import java.sql.SQLException;
import java.util.Optional;

import javax.naming.Context;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.openejb.resource.jdbc.dbcp.BasicDataSource;
import org.postgresql.Driver;


public class TestEventStoreDataSourceFactory {

    private static final String EVENT_STORE_URL_FORMAT = "jdbc:postgresql://localhost:5432/%s";
    private static final String EVENT_STORE_USER_NAME = "framework";
    private static final String EVENT_STORE_PASSWORD = "framework";
    private final Optional<String> liquibaseLocation;

    public TestEventStoreDataSourceFactory() {
        this.liquibaseLocation = Optional.empty();
    }

    public TestEventStoreDataSourceFactory(final String liquibaseLocation) {
        this.liquibaseLocation = Optional.of(liquibaseLocation);
    }

    public DataSource createDataSource(final String databaseName) throws LiquibaseException, SQLException {

        final BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setJdbcDriver(Driver.class.getName());
        basicDataSource.setJdbcUrl(format(EVENT_STORE_URL_FORMAT, databaseName));
        basicDataSource.setUserName(EVENT_STORE_USER_NAME);
        basicDataSource.setPassword(EVENT_STORE_PASSWORD);

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES,
                "org.apache.naming");

        if (liquibaseLocation.isPresent()) {
            initDatabase(basicDataSource);
        }

        return basicDataSource;
    }

    private void initDatabase(final DataSource dataSource) throws LiquibaseException, SQLException {
        final Liquibase liquibase = new Liquibase(
                liquibaseLocation.get(),
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
    }
}
