package uk.gov.justice.services.fileservice.datasource;

import javax.sql.DataSource;

public interface DataSourceProvider {

    DataSource getDataSource();
}
