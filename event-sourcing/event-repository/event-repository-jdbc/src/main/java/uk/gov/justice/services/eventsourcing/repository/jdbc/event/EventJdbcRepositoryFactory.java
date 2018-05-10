package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventJdbcRepositoryFactory {

    @Inject
    EventInsertionStrategy eventInsertionStrategy;

    @Inject
    JdbcRepositoryHelper jdbcRepositoryHelper;

    @Inject
    JdbcDataSourceProvider jdbcDataSourceProvider;

    public EventJdbcRepository eventJdbcRepository(final String jndiDatasource) {
        return new EventJdbcRepository(
                eventInsertionStrategy,
                jdbcRepositoryHelper,
                jdbcDataSourceProvider,
                jndiDatasource,
                getLogger(EventJdbcRepository.class));
    }
}
