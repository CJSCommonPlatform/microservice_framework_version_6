package uk.gov.justice.services.fileservice.datasource;

import javax.sql.DataSource;

/**
 * interface for getting the database datasource. By default the implementation will be
 * {@link JndiDataSourceProvider} for getting the datasource inside the container. For tests
 * then the datasource will be a hard JDBC Connection datasource.
 *
 */
public interface DataSourceProvider {

    /**
     * Gets the database data source
     *
     * @return the database data source
     */
    DataSource getDataSource();
}
