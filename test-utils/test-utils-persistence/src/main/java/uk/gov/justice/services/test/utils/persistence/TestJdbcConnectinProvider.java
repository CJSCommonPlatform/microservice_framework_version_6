package uk.gov.justice.services.test.utils.persistence;

import static java.lang.String.format;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestJdbcConnectinProvider {

    public Connection getEventStoreConnection(final String contextName) {

        final String host = getHost();

        final String url = format("jdbc:postgresql://%s/%seventstore", host, contextName);
        final String username = contextName;
        final String password = contextName;

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            final String message = format("Failed to get JDBC connection to %s Event Store. url: '%s', username '%s', password '%s'",
                    contextName,
                    url,
                    username,
                    password);

            throw new DataAccessException(message, e);
        }
    }

    public Connection getViewStoreConnection(final String contextName) {

        final String host = getHost();
        final String url = format("jdbc:postgresql://" + host + "/%sviewstore", contextName);
        final String username = contextName;
        final String password = contextName;

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            final String message = format("Failed to get JDBC connection to %s View Store. url: '%s', username '%s', password '%s'",
                    contextName,
                    url,
                    username,
                    password);

            throw new DataAccessException(message, e);
        }
    }
}
