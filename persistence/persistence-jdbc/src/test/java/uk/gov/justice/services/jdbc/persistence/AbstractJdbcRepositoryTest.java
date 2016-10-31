package uk.gov.justice.services.jdbc.persistence;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractJdbcRepositoryTest {

    @Mock
    private PreparedStatementWrapper ps;

    @Mock
    private ResultSet rs;


    @InjectMocks
    private AbstractJdbcRepository<String> repository = new TestJdbcRepository();


    @Test
    public void shouldTransformResultSetIntoStream() throws Exception {

        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        when(rs.getString(0)).thenReturn("aaa").thenReturn("bbb");
        final Stream<String> stream = repository.streamOf(ps);

        final List<String> list = stream.collect(toList());

        assertThat(list, hasSize(2));
        assertThat(list.get(0), is("aaa"));
        assertThat(list.get(1), is("bbb"));

    }

    @Test
    public void shouldCloseStatementIfEndOfResultSetReached() throws Exception {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(false);


        final Stream<String> stream = repository.streamOf(ps);
        stream.forEach(s->{});

        verify(ps).close();
    }

    @Test
    public void shouldNotCloseStatementIfEndOfResultSetNotReached() throws Exception {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(false);

        when(rs.getString(0)).thenReturn("");
        final Stream<String> stream = repository.streamOf(ps);


        stream.findFirst();
        verify(ps, never()).close();

    }


    private static class TestJdbcRepository extends AbstractJdbcRepository<String> {

        @Override
        protected String jndiName() throws NamingException {
            return null;
        }

        @Override
        protected String entityFrom(final ResultSet rs) throws SQLException {
            return rs.getString(0);
        }
    }

}