package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.naming.NamingException;
import javax.sql.DataSource;

public class EventRepositoryFactory {

    public static EventJdbcRepository getEventJdbcRepository(final DataSource dataSource) throws NamingException {
        final EventJdbcRepository eventJdbcRepository = new EventJdbcRepository();
        eventJdbcRepository.dataSource = dataSource;
        eventJdbcRepository.jdbcDataSourceProvider = new JdbcDataSourceProvider();
        eventJdbcRepository.logger = getLogger(EventJdbcRepository.class);
        eventJdbcRepository.jdbcRepositoryHelper = new JdbcRepositoryHelper();
        eventJdbcRepository.eventInsertionStrategy = new AnsiSQLEventLogInsertionStrategy();

        return eventJdbcRepository;
    }
}
