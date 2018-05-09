package uk.gov.justice.services.test.utils.persistence;

import static org.junit.Assert.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import liquibase.exception.LiquibaseException;
import org.h2.jdbcx.JdbcDataSource;
import org.hamcrest.Matchers;
import org.junit.Test;

public class TestDataSourceFactoryTest {

    private static final String LIQUIBASE_TEST_DB_CHANGELOG_XML = "liquibase/test-db-changelog.xml";

    final String sqlInsert = "INSERT INTO test_table (id, name) VALUES (1, 'test');";
    final String slqSelect = "SELECT * FROM test_table;";

    @Test
    public void shouldCreateJdbcDataSource() throws SQLException, LiquibaseException {

        final TestDataSourceFactory testDataSourceFactory = new TestDataSourceFactory(LIQUIBASE_TEST_DB_CHANGELOG_XML);
        final JdbcDataSource dataSource = testDataSourceFactory.createDataSource();

        try(final Statement statement = dataSource.getConnection().createStatement();) {
            statement.executeUpdate(sqlInsert);
            final ResultSet resultSet = statement.executeQuery(slqSelect);
            resultSet.next();
            assertThat(resultSet.getString("name"), Matchers.is("test"));
        } catch (final SQLException e) {
            throw e;
        }
    }
}