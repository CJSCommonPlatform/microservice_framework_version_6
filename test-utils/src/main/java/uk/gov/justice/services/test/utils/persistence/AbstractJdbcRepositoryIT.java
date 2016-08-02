package uk.gov.justice.services.test.utils.persistence;

import org.h2.jdbcx.JdbcDataSource;

import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;
import uk.gov.justice.services.test.utils.reflection.ReflectionUtils;

import javax.naming.Context;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;


public class AbstractJdbcRepositoryIT<T extends AbstractJdbcRepository> {
    private JdbcDataSource dataSource;
    protected T jdbcRepository;
    private final String liquibaseLocation;

    public AbstractJdbcRepositoryIT(String liquibaseLocation) {

        this.liquibaseLocation = liquibaseLocation;
    }

    protected void registerDataSource() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES,
                "org.apache.naming");

        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:./test;MV_STORE=FALSE;MVCC=FALSE");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");

        ReflectionUtils.setField(jdbcRepository, "datasource", dataSource);
        initDatabase();
    }

    private void initDatabase() throws Exception {
        Liquibase liquibase = new Liquibase(liquibaseLocation,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
    }
}
