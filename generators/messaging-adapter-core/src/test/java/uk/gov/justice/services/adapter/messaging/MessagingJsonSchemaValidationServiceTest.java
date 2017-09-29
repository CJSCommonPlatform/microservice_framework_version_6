package uk.gov.justice.services.adapter.messaging;

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

import javax.jms.TextMessage;

import org.everit.json.schema.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class MessagingJsonSchemaValidationServiceTest {

    private static final String COMPONENT = "COMMAND_API";

    @Mock
    Logger logger;

    @Mock
    private JsonValidationLoggerHelper jsonValidationLoggerHelper;

    @Mock
    private JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Mock
    private JsonSchemaValidator validator;

    @Mock
    private EventFilter eventFilter;

    @InjectMocks
    private MessagingJsonSchemaValidationService service;

    @Test
    public void shouldValidateMessage() throws Exception {
        final TextMessage message = mock(TextMessage.class);
        final String payload = "test payload";
        final String name = "test-name";

        when(message.getText()).thenReturn(payload);
        when(message.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(name);
        when(eventFilter.accepts(name)).thenReturn(true);

        service.validate(message, COMPONENT);

        verify(validator).validate(payload, "command_api/test-name");
    }

    @Test
    public void shouldNotValidateUnsupportedMessage() throws Exception {
        final TextMessage message = mock(TextMessage.class);
        final String payload = "test payload";
        final String name = "test-name-abc";

        when(message.getText()).thenReturn(payload);
        when(message.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(name);
        when(eventFilter.accepts(name)).thenReturn(false);

        service.validate(message, COMPONENT);

        verifyZeroInteractions(validator);
    }

    @Test
    public void shouldFallbackToOldJsonSchemasOnFirstValidationFailure() throws Exception {
        final TextMessage message = mock(TextMessage.class);
        final String payload = "test payload";
        final String name = "test-name";

        when(message.getText()).thenReturn(payload);
        when(message.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(name);
        when(eventFilter.accepts(name)).thenReturn(true);
        doThrow(mock(ValidationException.class)).when(validator).validate(payload, "command_api/test-name");

        service.validate(message, COMPONENT);

        verify(validator).validate(payload, "command_api/test-name");
    }

    @Test(expected = JsonSchemaValidationException.class)
    public void shouldThrowExceptionIfValidatorFails() throws Exception {
        final TextMessage message = mock(TextMessage.class);
        final String payload = "test payload";
        final String name = "test-name";

        when(message.getText()).thenReturn(payload);
        when(message.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(name);
        when(eventFilter.accepts(name)).thenReturn(true);

        doThrow(mock(ValidationException.class)).when(validator).validate(payload, "command_api/test-name");
        doThrow(mock(ValidationException.class)).when(validator).validate(payload, "test-name");

        service.validate(message, COMPONENT);
    }
}