package uk.gov.justice.services.test.utils.persistence;

import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.h2.jdbcx.JdbcDataSource;


public class TestDataSourceFactory {

    private static final String H2_CONFIG_URL = "jdbc:h2:mem:test;MV_STORE=FALSE;MVCC=FALSE;LOCK_MODE=3;DB_CLOSE_ON_EXIT=TRUE";
    private JdbcDataSource dataSource;
    private final String liquibaseLocation;


    public TestDataSourceFactory(final String liquibaseLocation) {
        this.liquibaseLocation = liquibaseLocation;
    }

    public JdbcDataSource createDataSource() throws LiquibaseException, SQLException {

        return createDataSource(H2_CONFIG_URL);
    }

    public JdbcDataSource createDataSource(String h2ConfigUrl) throws LiquibaseException, SQLException {

        return createDataSource(h2ConfigUrl, defaultSystemProperties(), "sa", "sa");
    }

    private Properties defaultSystemProperties() {
        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        properties.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
        return properties;
    }

    public JdbcDataSource createDataSource(final String h2ConfigUrl, final Properties systemProperties, String userName, String password) throws LiquibaseException, SQLException {

        System.getProperties().putAll(systemProperties);

        dataSource = new JdbcDataSource();
        dataSource.setURL(h2ConfigUrl);
        dataSource.setUser(userName);
        dataSource.setPassword(password);


        initDatabase();
        return dataSource;
    }

    private void initDatabase() throws LiquibaseException, SQLException {
        final Liquibase liquibase = new Liquibase(liquibaseLocation,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
    }
}
