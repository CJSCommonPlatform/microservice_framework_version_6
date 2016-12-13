package uk.gov.justice.services.example.cakeshop.it.util;

import uk.gov.justice.services.eventsourcing.jdbc.snapshot.jdbc.snapshot.SnapshotJdbcRepository;

import javax.sql.DataSource;


public class StandaloneSnapshotJdbcRepository extends SnapshotJdbcRepository {
    private final DataSource datasource;

    public StandaloneSnapshotJdbcRepository(DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    protected DataSource getDataSource() {
        return datasource;
    }


}
