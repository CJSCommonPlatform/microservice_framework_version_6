package uk.gov.justice.services.event.buffer.core.repository.streamstatus;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.sql.DataSource;

public class StandaloneStreamStatusJdbcRepositoryFactory {

    public static StreamStatusJdbcRepository getSnapshotStreamStatusJdbcRepository(final DataSource dataSource) {
        final StreamStatusJdbcRepository snapshotJdbcRepository = new StreamStatusJdbcRepository();
        snapshotJdbcRepository.dataSource = dataSource;
        snapshotJdbcRepository.jdbcRepositoryHelper = new JdbcRepositoryHelper();
        return snapshotJdbcRepository;
    }
}
