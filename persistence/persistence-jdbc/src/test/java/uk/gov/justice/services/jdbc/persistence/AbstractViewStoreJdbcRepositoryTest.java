package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;


public class AbstractViewStoreJdbcRepositoryTest {

    private AbstractViewStoreJdbcRepository viewStoreRepo = new AbstractViewStoreJdbcRepository(){
        @Override
        protected Object entityFrom(final ResultSet rs) throws SQLException {
            return null;
        }
    };

    @Test
    public void shouldConstructJndiName() throws Exception {
        viewStoreRepo.warFileName = "contextABC-some-other-stuff";
        assertThat(viewStoreRepo.jndiName(), is("java:/DS.contextABC"));
    }
}
