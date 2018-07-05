package uk.gov.justice.services.event.buffer.api;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

public class DataSourceFactory {

    public DataSource createDataSource() {

        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setPortNumber(5432);
        dataSource.setDatabaseName("frameworkviewstore");
        dataSource.setUser("framework");
        dataSource.setPassword("framework");

        return dataSource;
    }
}
