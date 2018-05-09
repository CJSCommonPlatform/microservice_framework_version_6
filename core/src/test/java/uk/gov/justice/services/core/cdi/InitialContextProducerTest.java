package uk.gov.justice.services.core.cdi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InitialContextProducerTest {

    @InjectMocks
    private InitialContextProducer initialContextProducer;

    @Test
    public void shouldReturnInitialContext() throws Exception {
        final InjectionPoint injectionPoint = mock(InjectionPoint.class);

        assertThat(initialContextProducer.initialContext(injectionPoint), is(instanceOf(InitialContext.class)));
    }

    @Test
    public void shouldDisposeOfAnInitialContext() throws Exception {

        final InitialContext initialContext = mock(InitialContext.class);

        initialContextProducer.close(initialContext);

        verify(initialContext).close();
    }

    @Test
    public void shouldFailIfClosingTheInitialContextFails() throws Exception {

        final NamingException namingException = new NamingException("Ooops");

        final InitialContext initialContext = mock(InitialContext.class);

        doThrow(namingException).when(initialContext).close();

        try {
            initialContextProducer.close(initialContext);

        } catch (final InjectionException expected) {
            assertThat(expected.getCause(), is(namingException));
            assertThat(expected.getMessage(), is("Failed to close InitialContext"));
        }
    }
}
