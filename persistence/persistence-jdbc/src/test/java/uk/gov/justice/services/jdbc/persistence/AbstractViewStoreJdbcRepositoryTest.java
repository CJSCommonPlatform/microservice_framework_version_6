package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class AbstractViewStoreJdbcRepositoryTest {


    private AbstractViewStoreJdbcRepository viewStoreRepo = new AbstractViewStoreJdbcRepository(){};

    @Test
    public void shouldConstructJndiName() throws Exception {
        viewStoreRepo.warFileName = "contextABC-some-other-stuff";
        assertThat(viewStoreRepo.jndiName(), is("java:/DS.contextABC"));
    }
}
