package uk.gov.justice.services.messaging.jms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.jms.exception.JmsConverterException;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEnvelopeConverterTest {

    private static final String MESSAGE_TEXT = "Test Message";
    private static final String NAME = "name";

    @InjectMocks
    private DefaultEnvelopeConverter envelopeConverter;

    @Mock
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @Mock
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Mock
    private TextMessage textMessage;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private Metadata metadata;

    @Mock
    private JsonObject messageAsJsonObject;

    @Mock
    private Session session;

    @Test
    public void shouldReturnEnvelope() throws Exception {
        when(textMessage.getText()).thenReturn(MESSAGE_TEXT);
        when(stringToJsonObjectConverter.convert(MESSAGE_TEXT)).thenReturn(messageAsJsonObject);
        when(jsonObjectEnvelopeConverter.asEnvelope(messageAsJsonObject)).thenReturn(envelope);

        JsonEnvelope actualEnvelope = envelopeConverter.fromMessage(textMessage);

        assertThat(actualEnvelope, equalTo(envelope));
    }

    @Test
    public void shouldReturnMessage() throws Exception {
        when(jsonObjectEnvelopeConverter.asJsonString(envelope)).thenReturn(MESSAGE_TEXT);
        when(session.createTextMessage(MESSAGE_TEXT)).thenReturn(textMessage);
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);

        TextMessage actualTextMessage = envelopeConverter.toMessage(envelope, session);

        assertThat(actualTextMessage, equalTo(textMessage));
        verify(textMessage).setStringProperty(JMS_HEADER_CPPNAME, NAME);
    }

    @Test(expected = JmsConverterException.class)
    public void shouldThrowExceptionWhenFailToRetrieveMessageContent() throws JMSException {
        doThrow(JMSException.class).when(textMessage).getText();

        envelopeConverter.fromMessage(textMessage);
    }

    @Test(expected = JmsConverterException.class)
    public void shouldThrowExceptionWhenFailToRetrieveMessageId() throws JMSException {
        doThrow(JMSException.class).when(textMessage).getText();
        doThrow(JMSException.class).when(textMessage).getJMSMessageID();

        envelopeConverter.fromMessage(textMessage);
    }

    @Test(expected = JmsConverterException.class)
    public void shouldThrowExceptionWhenFailToCreateTextMessage() throws JMSException {
        when(jsonObjectEnvelopeConverter.asJsonString(envelope)).thenReturn(MESSAGE_TEXT);
        doThrow(JMSException.class).when(session).createTextMessage(MESSAGE_TEXT);

        envelopeConverter.toMessage(envelope, session);
    }
}
