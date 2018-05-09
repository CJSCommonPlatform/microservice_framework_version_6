package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventStreamJdbcRepositoryFactory {

    @Inject
    JdbcRepositoryHelper eventStreamJdbcRepositoryHelper;

    @Inject
    JdbcDataSourceProvider jdbcDataSourceProvider;

    @Inject
    UtcClock clock;

    public EventStreamJdbcRepository eventStreamJdbcRepository(final String jndiDatasource) {
        return new EventStreamJdbcRepository(
                eventStreamJdbcRepositoryHelper,
                jdbcDataSourceProvider,
                clock,
                jndiDatasource,
                getLogger(EventStreamJdbcRepository.class));
    }
}
