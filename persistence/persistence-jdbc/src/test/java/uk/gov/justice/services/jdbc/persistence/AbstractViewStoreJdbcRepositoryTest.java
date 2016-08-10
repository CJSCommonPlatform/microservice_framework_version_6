package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.naming.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


public class AbstractViewStoreJdbcRepositoryTest {


    private AbstractViewStoreJdbcRepository viewStoreRepo = new AbstractViewStoreJdbcRepository(){};

    @Test
    public void shouldConstructJndiName() throws Exception {
        viewStoreRepo.warFileName = "contextABC-some-other-stuff";
        assertThat(viewStoreRepo.jndiName(), is("java:/DS.contextABC"));
    }
}
