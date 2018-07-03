package uk.gov.justice.services.example.cakeshop.it.helpers;

import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.sql.DataSource;

public class StandaloneSubscriptionJdbcRepositoryFactory {

    public SubscriptionJdbcRepository getSnapshotSubscriptionJdbcRepository(final DataSource dataSource) {
        final SubscriptionJdbcRepository snapshotJdbcRepository = new SubscriptionJdbcRepository();

        setField(snapshotJdbcRepository, "dataSource", dataSource);
        setField(snapshotJdbcRepository, "jdbcRepositoryHelper", new JdbcRepositoryHelper());

        return snapshotJdbcRepository;
    }
}
