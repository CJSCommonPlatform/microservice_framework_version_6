package uk.gov.justice.services.test.utils.core.eventsource;


import static java.lang.String.format;
import static java.sql.DriverManager.getDriver;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogJdbcRepository;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Standalone repository class to access event streams. To be used in integration testing
 */
public class TestEventLogRepository extends EventLogJdbcRepository {
    static final String SQL_FIND_ALL = "SELECT * FROM event_log";

    private final DataSource datasource;

    public TestEventLogRepository(final DataSource datasource) {
        this.datasource = datasource;
    }

    public TestEventLogRepository(final String url, final String username, final String password, final String driverClassName) {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        this.datasource = dataSource;
    }

    public TestEventLogRepository(final String contextName) throws SQLException {
        this(jdbcUrlFrom(contextName), contextName, contextName, getDriver(jdbcUrlFrom(contextName)).getClass().getName());
    }

    public static TestEventLogRepository forContext(final String contextName) {
        try {
            return new TestEventLogRepository(contextName);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
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
