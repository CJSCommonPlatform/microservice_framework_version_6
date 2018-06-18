package uk.gov.justice.services.eventsourcing.jdbc.snapshot;

import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import javax.sql.DataSource;


public class StandaloneSnapshotJdbcRepositoryFactory {

    public SnapshotJdbcRepository getSnapshotJdbcRepository(final DataSource dataSource) {

        final SnapshotJdbcRepository snapshotJdbcRepository = new SnapshotJdbcRepository();

        setField(snapshotJdbcRepository, "dataSource", dataSource);

        return snapshotJdbcRepository;
    }
}
