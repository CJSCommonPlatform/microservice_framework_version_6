package uk.gov.justice.services.example.cakeshop.it.util;


import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;

import javax.sql.DataSource;

public class StandaloneStreamStatusJdbcRepository extends StreamStatusJdbcRepository {
    private final DataSource datasource;

    public StandaloneStreamStatusJdbcRepository(final DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    protected DataSource getDataSource() {
        return datasource;
    }
}
