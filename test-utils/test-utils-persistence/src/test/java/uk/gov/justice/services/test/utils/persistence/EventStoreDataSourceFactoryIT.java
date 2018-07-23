package uk.gov.justice.services.test.utils.persistence;

import static org.junit.Assert.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import liquibase.exception.LiquibaseException;
import org.hamcrest.Matchers;
import org.junit.Test;

public class EventStoreDataSourceFactoryIT {

    private static final String LIQUIBASE_TEST_DB_CHANGELOG_XML = "liquibase/test-db-changelog.xml";

    final String sqlInsert = "INSERT INTO test_table (id, name) VALUES (1, 'test');";
    final String slqSelect = "SELECT * FROM test_table;";

    @Test
    public void shouldCreateJdbcDataSource() throws SQLException, LiquibaseException {

        final TestEventStoreDataSourceFactory testEventStoreDataSourceFactory = new TestEventStoreDataSourceFactory(LIQUIBASE_TEST_DB_CHANGELOG_XML);
        final DataSource dataSource = testEventStoreDataSourceFactory.createDataSource("frameworkviewstore");

        try (final Statement statement = dataSource.getConnection().createStatement();) {
            statement.executeUpdate(sqlInsert);
            final ResultSet resultSet = statement.executeQuery(slqSelect);
            resultSet.next();
            assertThat(resultSet.getString("name"), Matchers.is("test"));
        }
    }
}
