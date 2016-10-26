package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PreparedStatementWrapperTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Test
    public void shouldCloseConnectionWhenPreparedStatementCreationCausesException() throws SQLException {
        final String query = "someQuery";
        when(connection.prepareStatement(query)).thenThrow(new SQLException());
        try {
            PreparedStatementWrapper.valueOf(connection, query);
        } catch (Exception e) {

        }

        verify(connection).close();
    }

    @Test
    public void shouldCloseConnectionAndStatementWhenQueryExecutionThrowsException() throws SQLException {
        final String query = "someQuery2";
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new SQLException());
        final PreparedStatementWrapper ps = PreparedStatementWrapper.valueOf(connection, query);
        try {
            ps.executeQuery();
        } catch (Exception e) {

        }

        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldCloseConnectionStatementAndResultset() throws SQLException {
        final String query = "someQuery3";
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);


        final PreparedStatementWrapper ps = PreparedStatementWrapper.valueOf(connection, query);
        ps.executeQuery();

        ps.close();
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @Test
    public void shouldDelegateSetObjectMethodCall() throws SQLException {
        final String query = "dummy";
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        final PreparedStatementWrapper ps = PreparedStatementWrapper.valueOf(connection, query);
        final int parameterIndex = 1;
        final Object obj = new Object();
        ps.setObject(parameterIndex, obj);

        verify(preparedStatement).setObject(parameterIndex, obj);
    }

    @Test
    public void shouldDelegateSetStringMethodCall() throws SQLException {
        final String query = "dummy";
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        final PreparedStatementWrapper ps = PreparedStatementWrapper.valueOf(connection, query);
        final int parameterIndex = 2;
        final String str = "aaa";
        ps.setString(parameterIndex, str);

        verify(preparedStatement).setString(parameterIndex, str);
    }

    @Test
    public void shouldDelegateSetLongMethodCall() throws SQLException {
        final String query = "dummy";
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        final PreparedStatementWrapper ps = PreparedStatementWrapper.valueOf(connection, query);
        final int parameterIndex = 2;
        final Long lng = 123l;
        ps.setLong(parameterIndex, lng);

        verify(preparedStatement).setLong(parameterIndex, lng);
    }

    @Test
    public void shouldDelegateExecuteUpdateMethodCall() throws SQLException {
        final String query = "someQuery3";
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(4);


        final PreparedStatementWrapper ps = PreparedStatementWrapper.valueOf(connection, query);
        assertThat(ps.executeUpdate(), is(4));
    }

    @Test
    public void shouldCloseStatementOnExceptionOnSetObject() throws SQLException {
        final String query = "dummy";
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        final PreparedStatementWrapper ps = PreparedStatementWrapper.valueOf(connection, query);
        final int parameterIndex = 1;
        final Object obj = new Object();
        doThrow(new SQLException()).when(preparedStatement).setObject(parameterIndex, obj);
        try {
            ps.setObject(parameterIndex, obj);
        } catch (Exception e) {

        }
        verify(connection).close();
        verify(preparedStatement).close();


    }

    @Test
    public void shouldCloseStatementOnExceptionOnSetString() throws SQLException {
        final String query = "dummy";
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        final PreparedStatementWrapper ps = PreparedStatementWrapper.valueOf(connection, query);
        final int parameterIndex = 1;
        final String str = "aaa";
        doThrow(new SQLException()).when(preparedStatement).setString(parameterIndex, str);
        try {
            ps.setString(parameterIndex, str);
        } catch (Exception e) {

        }
        verify(connection).close();
        verify(preparedStatement).close();

    }


    @Test
    public void shouldCloseStatementOnExceptionOnSetLong() throws SQLException {
        final String query = "dummy";
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        final PreparedStatementWrapper ps = PreparedStatementWrapper.valueOf(connection, query);
        final int parameterIndex = 1;
        final Long lng = 5l;
        doThrow(new SQLException()).when(preparedStatement).setLong(parameterIndex, lng);
        try {
            ps.setLong(parameterIndex, lng);
        } catch (Exception e) {

        }
        verify(connection).close();
        verify(preparedStatement).close();

    }


}