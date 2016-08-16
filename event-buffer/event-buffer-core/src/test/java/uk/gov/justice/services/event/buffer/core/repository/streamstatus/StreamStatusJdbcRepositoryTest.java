package uk.gov.justice.services.event.buffer.core.repository.streamstatus;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
public class StreamStatusJdbcRepositoryTest {

    @InjectMocks
    private StreamStatusJdbcRepository repository;

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
    }

    @Test
    public void shouldSendInsertQueryIfDbIsPostgres9_5() throws Exception {
        //this is postgreSQL only, so we can't write repository level integration test,


        when(dbMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(dbMetadata.getDatabaseMajorVersion()).thenReturn(9);
        when(dbMetadata.getDatabaseMinorVersion()).thenReturn(5);


        when(connection.prepareStatement("INSERT INTO stream_status (version, stream_id) VALUES(?, ?) ON CONFLICT DO NOTHING"))
                .thenReturn(statement);

        final UUID streamId = randomUUID();
        final long version = 1l;
        repository.tryInsertingInPostgres95(new StreamStatus(streamId, version));

        verify(statement).setLong(1, version);
        verify(statement).setObject(2, streamId);
        verify(statement).executeUpdate();
    }

    @Test
    public void shouldSendInsertQueryIfDbIsPostgres10_0() throws Exception {

        when(dbMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(dbMetadata.getDatabaseMajorVersion()).thenReturn(10);
        when(dbMetadata.getDatabaseMinorVersion()).thenReturn(0);


        when(connection.prepareStatement("INSERT INTO stream_status (version, stream_id) VALUES(?, ?) ON CONFLICT DO NOTHING"))
                .thenReturn(statement);

        final UUID streamId = randomUUID();
        final long version = 1l;
        repository.tryInsertingInPostgres95(new StreamStatus(streamId, version));

        verify(statement).setLong(1, version);
        verify(statement).setObject(2, streamId);
        verify(statement).executeUpdate();
    }

    @Test
    public void shouldNotExecuteStatementIfDbIsNotPostgres() throws Exception {
        when(dbMetadata.getDatabaseProductName()).thenReturn("H2");

        repository.tryInsertingInPostgres95(new StreamStatus(randomUUID(), 1l));
        verify(connection).getMetaData();
        verify(connection).close();
        verifyNoMoreInteractions(connection);

    }

    @Test
    public void shouldNotExecuteStatementIfVersion9_4() throws Exception {
        when(dbMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(dbMetadata.getDatabaseMajorVersion()).thenReturn(9);
        when(dbMetadata.getDatabaseMinorVersion()).thenReturn(4);


        repository.tryInsertingInPostgres95(new StreamStatus(randomUUID(), 1l));
        verify(connection).getMetaData();
        verify(connection).close();
        verifyNoMoreInteractions(connection);

    }

    @Test
    public void shouldNotExecuteStatementIfVersion8_5() throws Exception {
        when(dbMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(dbMetadata.getDatabaseMajorVersion()).thenReturn(8);
        when(dbMetadata.getDatabaseMinorVersion()).thenReturn(5);


        repository.tryInsertingInPostgres95(new StreamStatus(randomUUID(), 1l));
        verify(connection).getMetaData();
        verify(connection).close();
        verifyNoMoreInteractions(connection);

    }


}