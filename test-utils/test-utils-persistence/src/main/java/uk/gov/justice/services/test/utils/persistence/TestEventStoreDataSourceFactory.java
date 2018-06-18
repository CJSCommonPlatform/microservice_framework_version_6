package uk.gov.justice.services.test.utils.persistence;

import java.sql.SQLException;

import javax.naming.Context;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.openejb.resource.jdbc.dbcp.BasicDataSource;
import org.postgresql.Driver;


public class TestEventStoreDataSourceFactory {

    private static final String EVENT_STORE_URL = "jdbc:postgresql://localhost:5432/frameworkeventstore";
    private static final String EVENT_STORE_USER_NAME = "framework";
    private static final String EVENT_STORE_PASSWORD = "framework";
    private final String liquibaseLocation;


    public TestEventStoreDataSourceFactory(final String liquibaseLocation) {
        this.liquibaseLocation = liquibaseLocation;
    }

    public DataSource createDataSource() throws LiquibaseException, SQLException {

        final BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setJdbcDriver(Driver.class.getName());
        basicDataSource.setJdbcUrl(EVENT_STORE_URL);
        basicDataSource.setUserName(EVENT_STORE_USER_NAME);
        basicDataSource.setPassword(EVENT_STORE_PASSWORD);

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES,
                "org.apache.naming");

        initDatabase(basicDataSource);

        return basicDataSource;
    }

    private void initDatabase(final DataSource dataSource) throws LiquibaseException, SQLException {
        final Liquibase liquibase = new Liquibase(
                liquibaseLocation,
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
    }
}
