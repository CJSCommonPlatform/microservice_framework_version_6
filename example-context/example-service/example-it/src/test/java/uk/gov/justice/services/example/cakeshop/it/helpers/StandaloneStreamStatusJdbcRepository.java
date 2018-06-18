package uk.gov.justice.services.example.cakeshop.it.helpers;


import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;

import javax.sql.DataSource;

public class StandaloneStreamStatusJdbcRepository extends StreamStatusJdbcRepository {
    private final DataSource datasource;

    public StandaloneStreamStatusJdbcRepository(final DataSource datasource) {
        this.datasource = datasource;
    }

    protected DataSource getDataSource() {
        return datasource;
    }
}
