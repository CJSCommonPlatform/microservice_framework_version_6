package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.copyWithInput;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.copyWithOutput;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InterceptorContextTest {

    @Mock
    private JsonEnvelope input;

    @Mock
    private InjectionPoint injectionPoint;

    @Test
    public void shouldBuildInterceptorContextWithInputAndInjectionPoint() throws Exception {

        final InterceptorContext result = interceptorContextWithInput(input, injectionPoint);

        assertThat(result.inputEnvelope(), is(input));
        assertThat(result.injectionPoint(), is(injectionPoint));
        assertThat(result.outputEnvelope(), is(Optional.empty()));
    }

    @Test
    public void shouldCopyInterceptorContextWithInput() throws Exception {

        final JsonEnvelope expectedInput = mock(JsonEnvelope.class);
        final InterceptorContext initialInterceptorContext = interceptorContextWithInput(input, injectionPoint);

        final InterceptorContext result = copyWithInput(initialInterceptorContext, expectedInput);

        assertThat(result.inputEnvelope(), is(expectedInput));
        assertThat(result.injectionPoint(), is(injectionPoint));
        assertThat(result.outputEnvelope(), is(Optional.empty()));
    }

    @Test
    public void shouldCopyInterceptorContextWithOutput() throws Exception {

        final JsonEnvelope output = mock(JsonEnvelope.class);
        final InterceptorContext initialInterceptorContext = interceptorContextWithInput(input, injectionPoint);

        final InterceptorContext result = copyWithOutput(initialInterceptorContext, output);

        assertThat(result.inputEnvelope(), is(input));
        assertThat(result.injectionPoint(), is(injectionPoint));
        assertThat(result.outputEnvelope(), is(Optional.of(output)));
    }

    @Test
    public void shouldSetAndRetrieveInputParameter() throws Exception {

        final Object parameter = mock(Object.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(input, injectionPoint);

        interceptorContext.setInputParameter("test", parameter);

        assertThat(interceptorContext.getInputParameter("test"), is(Optional.of(parameter)));
    }

    @Test
    public void shouldSetAndRetrieveOutputParameter() throws Exception {

        final Object parameter = mock(Object.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(input, injectionPoint);

        interceptorContext.setOutputParameter("test", parameter);

        assertThat(interceptorContext.getOutputParameter("test"), is(Optional.of(parameter)));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfInputNotSet() throws Exception {
        interceptorContextWithInput(null, injectionPoint).inputEnvelope();
    }
}