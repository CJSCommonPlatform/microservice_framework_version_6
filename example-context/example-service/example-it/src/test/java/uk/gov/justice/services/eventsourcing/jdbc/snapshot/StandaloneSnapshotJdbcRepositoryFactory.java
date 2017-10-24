package uk.gov.justice.services.eventsourcing.jdbc.snapshot;

import javax.sql.DataSource;


public class StandaloneSnapshotJdbcRepositoryFactory {

    public static SnapshotJdbcRepository getSnapshotJdbcRepository(final DataSource dataSource) {
        final SnapshotJdbcRepository snapshotJdbcRepository = new SnapshotJdbcRepository();
        snapshotJdbcRepository.dataSource = dataSource;
        return snapshotJdbcRepository;
    }
}
