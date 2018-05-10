package uk.gov.justice.services.test.utils.persistence;


import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TestJdbcConnectionProviderTest {

    @InjectMocks
    private TestJdbcConnectionProvider testJdbcConnectionProvider;

    @Test
    @Ignore("This can't work without a usergroups eventstore running on postgresql")
    public void shouldGetConnectionToEventStore() throws Exception {

        try(final Connection connection = testJdbcConnectionProvider.getEventStoreConnection("usersgroups")) {
            assertThat(connection, is(notNullValue()));
        }
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void shouldThrowADataAccessExceptionIfTheConnectionToTheEventFails() throws Exception {

        final String expectedErrorMessage =
                "Failed to get JDBC connection " +
                        "to my-non-existent-context Event Store. " +
                        "url: 'jdbc:postgresql://localhost/my-non-existent-contexteventstore', " +
                        "username 'my-non-existent-context', " +
                        "password 'my-non-existent-context'";

        try(final Connection connection = testJdbcConnectionProvider.getEventStoreConnection("my-non-existent-context")) {
            assertThat(connection, is(notNullValue()));
            fail();
        } catch(DataAccessException expected) {
            assertThat(expected.getCause(), is(instanceOf(SQLException.class)));
            assertThat(expected.getMessage(), is(expectedErrorMessage));
        }
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void shouldThrowADataAccessExceptionIfTheConnectionToTheViewStoreFails() throws Exception {

        final String expectedErrorMessage =
                "Failed to get JDBC connection " +
                        "to my-non-existent-context View Store. " +
                        "url: 'jdbc:postgresql://localhost/my-non-existent-contextviewstore', " +
                        "username 'my-non-existent-context', " +
                        "password 'my-non-existent-context'";

        try(final Connection connection = testJdbcConnectionProvider.getViewStoreConnection("my-non-existent-context")) {
            assertThat(connection, is(notNullValue()));
            fail();
        } catch(DataAccessException expected) {
            assertThat(expected.getCause(), is(instanceOf(SQLException.class)));
            assertThat(expected.getMessage(), is(expectedErrorMessage));
        }
    }
}
