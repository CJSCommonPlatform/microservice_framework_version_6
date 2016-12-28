package uk.gov.justice.services.fileservice.repository;

import javax.sql.DataSource;

public interface DataSourceProvider {

    DataSource getDataSource();
}
