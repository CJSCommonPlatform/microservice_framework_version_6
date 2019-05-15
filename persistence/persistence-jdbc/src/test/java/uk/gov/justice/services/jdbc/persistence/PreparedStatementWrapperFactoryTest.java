package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PreparedStatementWrapperFactoryTest {

    @InjectMocks
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Test
    public void shouldCreatePreparedStatementWrapper() throws Exception {

        final String query = "SELECT something FROM somewhere";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);


        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);

        final PreparedStatementWrapper preparedStatementWrapper = preparedStatementWrapperFactory.preparedStatementWrapperOf(
                dataSource,
                query);

        assertThat(getValueOfField(preparedStatementWrapper, "preparedStatement", PreparedStatement.class), is(preparedStatement));

        final LinkedList<AutoCloseable> closeables = getValueOfField(preparedStatementWrapper, "closeables", LinkedList.class);

        assertThat(closeables, hasItems(connection, preparedStatement));
    }
}
