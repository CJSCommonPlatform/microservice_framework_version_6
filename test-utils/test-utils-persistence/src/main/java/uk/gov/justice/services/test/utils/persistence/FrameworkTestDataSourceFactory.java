package uk.gov.justice.services.test.utils.persistence;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

/**
 * Utility for getting a DataSource to the Event Store, View Store or File Store
 */
public class FrameworkTestDataSourceFactory {

    private static final String EVENT_STORE_DATABASE_NAME = "frameworkeventstore";
    private static final String VIEW_STORE_DATABASE_NAME = "frameworkviewstore";
    private static final String FILE_STORE_DATABASE_NAME = "frameworkfilestore";

    private static final int PORT_NUMBER = 5432;
    private static final String USERNAME = "framework";
    private static final String PASSWORD = "framework";

    /**
     * Gets a DataSource to the Event Store
     *
     * @return a JDBC DataSource to the Event Store
     */
    public DataSource createEventStoreDataSource() {
        return createDataSource(EVENT_STORE_DATABASE_NAME);
    }

    /**
     * Gets a DataSource to the View Store
     *
     * @return a JDBC DataSource to the View Store
     */
    public DataSource createViewStoreDataSource() {
        return createDataSource(VIEW_STORE_DATABASE_NAME);
    }

    /**
     * Gets a DataSource to the File Store
     *
     * @return a JDBC DataSource to the File Store
     */
    public DataSource createFileStoreDataSource() {
        return createDataSource(FILE_STORE_DATABASE_NAME);
    }

    private DataSource createDataSource(final String databaseName) {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setPortNumber(PORT_NUMBER);
        dataSource.setDatabaseName(databaseName);
        dataSource.setUser(USERNAME);
        dataSource.setPassword(PASSWORD);

        return dataSource;
    }
}
