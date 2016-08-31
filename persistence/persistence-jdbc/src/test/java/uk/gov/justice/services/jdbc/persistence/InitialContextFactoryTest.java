package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.naming.InitialContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class InitialContextFactoryTest {

    private InitialContextFactory initialContextFactory = new InitialContextFactory();

    @Test
    public void shouldCreateANewInitialContext() throws Exception {

        assertThat(initialContextFactory.create(), is(instanceOf(InitialContext.class)));
    }
}
