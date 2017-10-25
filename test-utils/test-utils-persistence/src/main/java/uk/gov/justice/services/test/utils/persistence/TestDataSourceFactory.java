package uk.gov.justice.services.test.utils.persistence;

import java.sql.SQLException;

import javax.naming.Context;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.h2.jdbcx.JdbcDataSource;


public class TestDataSourceFactory {

    private JdbcDataSource dataSource;
    private final String liquibaseLocation;

    public TestDataSourceFactory(final String liquibaseLocation) {
        this.liquibaseLocation = liquibaseLocation;
    }

    public JdbcDataSource createDataSource() throws LiquibaseException, SQLException {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES,
                "org.apache.naming");

        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;MV_STORE=FALSE;MVCC=FALSE;DB_CLOSE_ON_EXIT=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");

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
