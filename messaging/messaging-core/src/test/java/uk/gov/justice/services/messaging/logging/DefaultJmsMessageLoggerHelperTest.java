package uk.gov.justice.services.messaging.logging;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJmsMessageLoggerHelperTest {

    private static final String A_NAME = "context.command.do-something";
    private static final String A_MESSAGE_ID = UUID.randomUUID().toString();
    private static final String A_SESSION_ID = UUID.randomUUID().toString();
    private static final String A_USER_ID = UUID.randomUUID().toString();

    @Mock
    private TextMessage message;

    @Mock
    private List<UUID> causation;

    @InjectMocks
    DefaultJmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Test
    public void shouldReturnValidMetadataJson() throws JMSException {
        when(message.getText()).thenReturn(envelopeString());

        JsonObject result = toJsonObject(message);

        assertThat(result.getString("id"), is(A_MESSAGE_ID));
        assertThat(result.getString("name"), is(A_NAME));
        assertThat(result.getJsonObject("context").getString("user"), is(A_USER_ID));
        assertThat(result.getJsonObject("context").getString("session"), is(A_SESSION_ID));
    }

    @Test
    public void shouldReturnErrorMessageOnError() throws JMSException {
        when(message.getText()).thenThrow(JMSException.class);

        assertThat(jmsMessageLoggerHelper.toJmsTraceString(message), containsString("Could not find: _metadata in message"));
    }

    private JsonObject toJsonObject(final Message message) {
        StringReader sw = new StringReader(jmsMessageLoggerHelper.toJmsTraceString(message));
        JsonReader jsonReader = Json.createReader(sw);
        return jsonReader.readObject();
    }

    private String envelopeString() {

        return createObjectBuilder()
                .add("_metadata", metadataOf(UUID.fromString(A_MESSAGE_ID), A_NAME)
                        .withUserId(A_USER_ID)
                        .withSessionId(A_SESSION_ID)
                        .build().asJsonObject())
                .add("something", "anything really")
                .build().toString();
    }

}