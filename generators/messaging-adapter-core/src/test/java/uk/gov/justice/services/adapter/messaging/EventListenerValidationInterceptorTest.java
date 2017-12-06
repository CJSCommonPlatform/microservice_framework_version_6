package uk.gov.justice.services.adapter.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonValidationLoggerHelper;
import uk.gov.justice.services.event.buffer.api.EventFilter;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;

import javax.interceptor.InvocationContext;
import javax.jms.TextMessage;

import org.everit.json.schema.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Unit tests for the {@link JsonSchemaValidationInterceptor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class EventListenerValidationInterceptorTest {

    @Mock
    Logger logger;

    @Mock
    JmsParameterChecker parametersChecker;

    @Mock
    private JsonSchemaValidator validator;

    @Mock
    private InvocationContext invocationContext;

    @Mock
    private JsonValidationLoggerHelper jsonValidationLoggerHelper;

    @Mock
    private JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Mock
    private EventFilter eventFilter;

    @InjectMocks
    private EventListenerValidationInterceptor interceptor;

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
        when(eventFilter.accepts(name)).thenReturn(true);
        when(invocationContext.getParameters()).thenReturn(new Object[]{message});

        interceptor.validate(invocationContext);

        verify(validator).validate(payload, name);
    }

    @Test
    public void shouldNotValidateUnsupportedMessage() throws Exception {
        final TextMessage message = mock(TextMessage.class);
        final String payload = "test payload";
        final String name = "test-name-abc";

        when(message.getText()).thenReturn(payload);
        when(message.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(name);
        when(eventFilter.accepts(name)).thenReturn(false);
        when(invocationContext.getParameters()).thenReturn(new Object[]{message});

        interceptor.validate(invocationContext);

        verifyZeroInteractions(validator);
    }

    @Test(expected = ValidationException.class)
    public void shouldThrowExceptionIfValidatorFails() throws Exception {
        final TextMessage message = mock(TextMessage.class);
        final String payload = "test payload";
        final String name = "test-name";

        when(message.getText()).thenReturn(payload);
        when(message.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(name);
        when(eventFilter.accepts(name)).thenReturn(true);
        when(invocationContext.getParameters()).thenReturn(new Object[]{message});
        doThrow(mock(ValidationException.class)).when(validator).validate(payload, name);

        interceptor.validate(invocationContext);
    }
}
