package uk.gov.justice.services.jdbc.persistence;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import javax.naming.NamingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
