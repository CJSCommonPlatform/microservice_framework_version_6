package uk.gov.justice.services.fileservice.repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.Test;

public class CloserTest {

    private final Closer closer = new Closer();

    @Test
    public void shouldCloseAllAutoCloseables() throws Exception {

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        closer.close(connection, preparedStatement);

        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldHandleNullAutoCloseables() throws Exception {

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        closer.close(null, connection, null, preparedStatement, null);

        verify(connection).close();
        verify(preparedStatement).close();
    }
}
