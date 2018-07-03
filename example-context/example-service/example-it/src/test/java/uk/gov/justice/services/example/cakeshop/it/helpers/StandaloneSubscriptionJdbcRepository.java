package uk.gov.justice.services.example.cakeshop.it.helpers;


import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;

import javax.sql.DataSource;

public class StandaloneSubscriptionJdbcRepository extends SubscriptionJdbcRepository {
    private final DataSource datasource;

    public StandaloneSubscriptionJdbcRepository(final DataSource datasource) {
        this.datasource = datasource;
    }

    protected DataSource getDataSource() {
        return datasource;
    }
}
