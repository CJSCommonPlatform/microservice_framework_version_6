package uk.gov.justice.services.adapter.messaging;

import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonValidationLoggerHelper;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;
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
public class JsonSchemaValidationInterceptorTest {

    @Mock
    Logger logger;

    @Mock
    JmsParameterChecker parametersChecker;

    @Mock
    private JsonSchemaValidator jsonSchemaValidator;

    @Mock
    private NameToMediaTypeConverter nameToMediaTypeConverter;

    @Mock
    private InvocationContext invocationContext;

    @Mock
    private JsonValidationLoggerHelper jsonValidationLoggerHelper;

    @Mock
    private JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @InjectMocks
    private JsonSchemaValidationInterceptor jsonSchemaValidationInterceptor;

    @Test
    public void shouldReturnContextProceed() throws Exception {

        final String payload = "{\"the\": \"payload\"}";

        final Object proceed = new Object();
        final TextMessage message = mock(TextMessage.class);
        final MediaType mediaType = mock(MediaType.class);

        final String actionName = message.getStringProperty(JMS_HEADER_CPPNAME);

        when(invocationContext.proceed()).thenReturn(proceed);
        when(invocationContext.getParameters()).thenReturn(new Object[]{message});
        when(message.getText()).thenReturn(payload);
        when(nameToMediaTypeConverter.convert(actionName)).thenReturn(mediaType);

        assertThat(jsonSchemaValidationInterceptor.validate(invocationContext), sameInstance(proceed));
    }

    @Test
    public void shouldValidateMessage() throws Exception {
        final TextMessage message = mock(TextMessage.class);
        final String payload = "test payload";
        final String name = "test-name";
        final MediaType mediaType = mock(MediaType.class);

        when(message.getText()).thenReturn(payload);
        when(message.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(name);
        when(invocationContext.getParameters()).thenReturn(new Object[]{message});
        when(nameToMediaTypeConverter.convert(name)).thenReturn(mediaType);

        jsonSchemaValidationInterceptor.validate(invocationContext);

        verify(jsonSchemaValidator).validate(payload, name, of(mediaType));
    }

    @Test(expected = ValidationException.class)
    public void shouldThrowExceptionIfValidatorFails() throws Exception {
        final TextMessage message = mock(TextMessage.class);
        final String payload = "test payload";
        final String name = "test-name";
        final MediaType mediaType = mock(MediaType.class);

        when(message.getText()).thenReturn(payload);
        when(message.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(name);
        when(invocationContext.getParameters()).thenReturn(new Object[]{message});
        when(nameToMediaTypeConverter.convert(name)).thenReturn(mediaType);

        doThrow(mock(ValidationException.class)).when(jsonSchemaValidator).validate(payload, name, of(mediaType));

        jsonSchemaValidationInterceptor.validate(invocationContext);
    }
}
