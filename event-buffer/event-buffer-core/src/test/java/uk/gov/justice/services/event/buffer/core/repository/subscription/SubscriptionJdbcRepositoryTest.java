package uk.gov.justice.services.event.buffer.core.repository.subscription;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionJdbcRepositoryTest {

    @InjectMocks
    private SubscriptionJdbcRepository repository;

    @Mock
    DataSource dataSource;

    @Mock
    Connection connection;

    @Mock
    DatabaseMetaData dbMetadata;

    @Mock
    PreparedStatement statement;

    @Before
    public void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(dbMetadata);
        repository.jdbcRepositoryHelper = new JdbcRepositoryHelper();
    }

    @Test
    public void shouldAttemptToInsert() throws Exception {
        //this is postgreSQL only, so we can't write repository level integration test,

        final String source = "a source";

        when(connection.prepareStatement("INSERT INTO subscription (latest_position, stream_id, source) VALUES (?, ?, ?) ON CONFLICT DO NOTHING"))
                .thenReturn(statement);

        final UUID streamId = randomUUID();
        final long version = 1l;
        repository.insertOrDoNothing(new Subscription(streamId, version, source));

        verify(statement).setLong(1, version);
        verify(statement).setObject(2, streamId);
        verify(statement).executeUpdate();
    }

}
