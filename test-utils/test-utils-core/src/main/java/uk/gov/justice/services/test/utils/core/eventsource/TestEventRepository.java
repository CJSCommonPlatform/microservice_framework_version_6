package uk.gov.justice.services.test.utils.core.eventsource;


import static java.lang.String.format;
import static java.sql.DriverManager.getDriver;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.LoggerFactory;

/**
 * Standalone repository class to access event streams. To be used in integration testing
 */
public class TestEventRepository extends EventJdbcRepository {

    private final DataSource datasource;

    public TestEventRepository(final DataSource datasource) {
        this.datasource = datasource;
        this.logger = LoggerFactory.getLogger(TestEventRepository.class);
    }

    public TestEventRepository(final String url, final String username, final String password, final String driverClassName) {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        this.datasource = dataSource;
        this.logger = LoggerFactory.getLogger(TestEventRepository.class);

    }

    public TestEventRepository(final String contextName) throws SQLException {
        this(jdbcUrlFrom(contextName), contextName, contextName, getDriver(jdbcUrlFrom(contextName)).getClass().getName());
    }

    public static TestEventRepository forContext(final String contextName) {
        try {
            return new TestEventRepository(contextName);
        } catch (SQLException e) {
            throw new IllegalArgumentException(format("Error instantiating repository for context: %s", contextName), e);
        }
    }

    private static String jdbcUrlFrom(final String contextName) {
        return format("jdbc:postgresql://%s/%seventstore", getHost(), contextName);
    }

    public List<Event> eventsOfStreamId(final UUID streamId) {
        try (final Stream<Event> events = this.findByStreamIdOrderBySequenceIdAsc(streamId)) {
            return events.collect(toList());
        }
    }

    public List<Event> allEvents() {
        try (final Stream<Event> events = this.findAll()) {
            return events.collect(toList());
        }
    }

    @Override
    protected DataSource getDataSource() {
        return datasource;
    }

}
