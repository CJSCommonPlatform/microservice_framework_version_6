package uk.gov.justice.services.adapter.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.core.json.JsonSchemaValidator;

import javax.interceptor.InvocationContext;
import javax.jms.TextMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link JsonSchemaValidationInterceptor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonSchemaValidationInterceptorTest {

    @Mock
    private JsonSchemaValidator validator;

    @Mock
    private InvocationContext invocationContext;

    @InjectMocks
    private JsonSchemaValidationInterceptor interceptor;

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfInvokedWithWrongNumberOfParameters() throws Exception {
        when(invocationContext.getParameters()).thenReturn(new Object[]{});
        interceptor.validate(invocationContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfInvokedWithParameterOtherThanTextMessage() throws Exception {
        when(invocationContext.getParameters()).thenReturn(new Object[]{new Object()});
        interceptor.validate(invocationContext);
    }

    @Test
    public void shouldReturnContextProceed() throws Exception {
        final Object proceed = new Object();
        final TextMessage message = mock(TextMessage.class);
        when(invocationContext.proceed()).thenReturn(proceed);
        when(invocationContext.getParameters()).thenReturn(new Object[]{message});

        assertThat(interceptor.validate(invocationContext), sameInstance(proceed));
    }

    @Test
    public void shouldValidateMessage() throws Exception {
        final TextMessage message = mock(TextMessage.class);
        final String payload = "test payload";
        final String name = "test-name";
        when(message.getText()).thenReturn(payload);
        when(message.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(name);
        when(invocationContext.getParameters()).thenReturn(new Object[]{message});

        interceptor.validate(invocationContext);

        verify(validator).validate(payload, name);
    }
}
