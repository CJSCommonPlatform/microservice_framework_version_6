package uk.gov.justice.services.test.utils.core.eventsource;


import static java.lang.String.format;
import static java.sql.DriverManager.getDriver;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogJdbcRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standalone repository class to access event streams. To be used in integration testing
 */
public class TestEventLogRepository extends EventLogJdbcRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestEventLogRepository.class);
    static final String SQL_FIND_ALL = "SELECT * FROM event_log";

    private final DataSource datasource;

    public TestEventLogRepository(final DataSource datasource) {
        this.datasource = datasource;
        this.logger = LOGGER;
    }

    public TestEventLogRepository(final String url, final String username, final String password, final String driverClassName) {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        this.datasource = dataSource;
        this.logger = LOGGER;

    }

    public TestEventLogRepository(final String contextName) throws SQLException {
        this(jdbcUrlFrom(contextName), contextName, contextName, getDriver(jdbcUrlFrom(contextName)).getClass().getName());
    }

    public static TestEventLogRepository forContext(final String contextName) {
        try {
            return new TestEventLogRepository(contextName);
        } catch (SQLException e) {
            throw new IllegalArgumentException(format("Error instantiating repository for context: %s", contextName), e);
        }
    }

    public List<EventLog> eventsOfStreamId(final UUID streamId) {
        try (final Stream<EventLog> events = this.findByStreamIdOrderBySequenceIdAsc(streamId)) {
            return events.collect(toList());
        }
    }

    public List<EventLog> allEvents() {
        try (final Stream<EventLog> events = this.findAll()) {
            return events.collect(toList());
        }
    }

    @Override
    protected DataSource getDataSource() {
        return datasource;
    }

    private static String jdbcUrlFrom(final String contextName) {
        return format("jdbc:postgresql://%s/%seventstore", getHost(), contextName);
    }

}
