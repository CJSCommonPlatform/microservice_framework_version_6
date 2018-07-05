package uk.gov.justice.services.test.utils.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.Test;


public class FrameworkTestDataSourceFactoryTest {

    private final FrameworkTestDataSourceFactory frameworkTestDataSourceFactory = new FrameworkTestDataSourceFactory();

    @Test
    public void shouldGetADataSouceToTheEventStore() throws Exception {

        final DataSource eventStoreDataSource = frameworkTestDataSourceFactory.createEventStoreDataSource();

        try (final Connection connection = eventStoreDataSource.getConnection();
             final ResultSet catalogs = connection.getMetaData().getCatalogs()) {
            while (catalogs.next()) {
                assertThat(catalogs.getString(1), is("frameworkeventstore"));
            }
        }
    }

    @Test
    public void shouldGetADataSouceToTheViewStore() throws Exception {

        final DataSource viewStoreDataSource = frameworkTestDataSourceFactory.createViewStoreDataSource();

        try (final Connection connection = viewStoreDataSource.getConnection();
             final ResultSet catalogs = connection.getMetaData().getCatalogs()) {
            while (catalogs.next()) {
                assertThat(catalogs.getString(1), is("frameworkviewstore"));
            }
        }
    }

    @Test
    public void shouldGetADataSouceToTheFileStore() throws Exception {

        final DataSource fileStoreDataSource = frameworkTestDataSourceFactory.createFileStoreDataSource();

        try (final Connection connection = fileStoreDataSource.getConnection();
             final ResultSet catalogs = connection.getMetaData().getCatalogs()) {
            while (catalogs.next()) {
                assertThat(catalogs.getString(1), is("frameworkfilestore"));
            }
        }
    }
}
