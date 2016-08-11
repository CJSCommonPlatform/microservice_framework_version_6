package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherTargetTest {

    @Mock
    Function<JsonEnvelope, JsonEnvelope> dispatcher;

    @InjectMocks
    DispatcherTarget dispatchInterceptor;

    @Test
    public void shouldCallDispatcherWhenProcessIsCalled() throws Exception {

        final JsonEnvelope input = mock(JsonEnvelope.class);
        final JsonEnvelope output = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(input, mock(InjectionPoint.class));

        when(dispatcher.apply(input)).thenReturn(output);

        final InterceptorContext result = dispatchInterceptor.process(interceptorContext);

        assertThat(result.inputEnvelope(), is(input));
        assertThat(result.outputEnvelope(), is(Optional.of(output)));
    }
}