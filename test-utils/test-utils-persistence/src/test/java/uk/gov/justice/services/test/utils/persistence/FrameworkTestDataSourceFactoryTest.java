package uk.gov.justice.services.test.utils.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Properties;

import javax.sql.DataSource;

import org.hamcrest.MatcherAssert;
import org.junit.Test;


public class FrameworkTestDataSourceFactoryTest {

    private final FrameworkTestDataSourceFactory frameworkTestDataSourceFactory = new FrameworkTestDataSourceFactory();

    @Test
    public void shouldGetADataSourceToTheEventStore() throws Exception {

        final DataSource eventStoreDataSource = frameworkTestDataSourceFactory.createEventStoreDataSource();

        try (final Connection connection = eventStoreDataSource.getConnection();
             final ResultSet catalogs = connection.getMetaData().getCatalogs()) {
            while (catalogs.next()) {
                assertThat(catalogs.getString(1), is("frameworkeventstore"));
            }
        }
    }

    @Test
    public void shouldGetADataSourceToTheViewStore() throws Exception {

        final DataSource viewStoreDataSource = frameworkTestDataSourceFactory.createViewStoreDataSource();

        try (final Connection connection = viewStoreDataSource.getConnection();
             final ResultSet catalogs = connection.getMetaData().getCatalogs()) {
            while (catalogs.next()) {
                assertThat(catalogs.getString(1), is("frameworkviewstore"));
            }
        }
    }

    @Test
    public void shouldGetADataSourceToTheFileStore() throws Exception {

        final DataSource fileStoreDataSource = frameworkTestDataSourceFactory.createFileStoreDataSource();

        try (final Connection connection = fileStoreDataSource.getConnection();
             final ResultSet catalogs = connection.getMetaData().getCatalogs()) {
            while (catalogs.next()) {
                assertThat(catalogs.getString(1), is("frameworkfilestore"));
            }
        }
    }

    @Test
    public void shouldLoadPropertiesSuccessfully(){
        Properties prop = frameworkTestDataSourceFactory.getTestDatSourceProperties();

        MatcherAssert.assertThat(prop.getProperty("PORT_NUMBER"), is("5432"));
        MatcherAssert.assertThat(prop.getProperty("USERNAME"), is("framework"));
        MatcherAssert.assertThat(prop.getProperty("PASSWORD"), is("framework"));
    }
}
