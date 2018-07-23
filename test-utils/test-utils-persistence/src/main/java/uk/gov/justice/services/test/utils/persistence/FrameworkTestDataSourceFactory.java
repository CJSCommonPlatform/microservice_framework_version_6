package uk.gov.justice.services.test.utils.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

/**
 * Utility for getting a DataSource to the Event Store, View Store or File Store
 */
public class FrameworkTestDataSourceFactory {

    private static final String EVENT_STORE_DATABASE_NAME = "frameworkeventstore";
    private static final String VIEW_STORE_DATABASE_NAME = "frameworkviewstore";
    private static final String FILE_STORE_DATABASE_NAME = "frameworkfilestore";
    private static final String DATA_SOURCE_CONFIG_PROPERTIES = "test-data-source.properties";

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

        final Properties prop = getTestDatSourceProperties();
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setPortNumber(Integer.parseInt(prop.getProperty("PORT_NUMBER")));
        dataSource.setDatabaseName(databaseName);
        dataSource.setUser(prop.getProperty("USERNAME"));
        dataSource.setPassword(prop.getProperty("PASSWORD"));

        return dataSource;
    }

    public Properties getTestDatSourceProperties(){
        final Properties prop = new Properties();

        try(final InputStream input = this.getClass().getClassLoader().getResourceAsStream(DATA_SOURCE_CONFIG_PROPERTIES)){
            if(input != null){
                prop.load(input);
            }
        }
        catch(IOException ex){
            ex.getMessage();
        }
        return prop;
    }
}
