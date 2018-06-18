package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.sql.DataSource;

public class EventRepositoryFactory {

    public EventJdbcRepository getEventJdbcRepository(final DataSource dataSource) throws IllegalAccessException {
        final EventJdbcRepository eventJdbcRepository = new EventJdbcRepository(
                new AnsiSQLEventLogInsertionStrategy(),
                new JdbcRepositoryHelper(),
                new JdbcDataSourceProvider(),
                null,
                getLogger(EventJdbcRepository.class));

        setField(eventJdbcRepository, "dataSource", dataSource);

        return eventJdbcRepository;
    }
}
