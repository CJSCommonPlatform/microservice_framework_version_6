package uk.gov.justice.services.messaging.logging;

import static javax.json.Json.createObjectBuilder;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CAUSATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.SESSION_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.USER_ID;
import static uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper.toJmsTraceString;
import static uk.gov.justice.services.messaging.logging.JsonEnvelopeLoggerHelperTest.makeCausations;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmsMessageLoggerHelperTest {

    private static final String A_CORRELATION_ID = UUID.randomUUID().toString();
    private static final String A_NAME = "context.command.do-something";
    private static final String A_MESSAGE_ID = UUID.randomUUID().toString();
    private static final String A_SESSION_ID = UUID.randomUUID().toString();
    private static final String A_USER_ID = UUID.randomUUID().toString();

    @Mock
    private TextMessage message;

    @Mock
    private List<UUID> causation;

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(JmsMessageLoggerHelper.class);
    }

    @Test
    public void shouldReturnValidMetadataJson() throws JMSException {
        when(message.getText()).thenReturn(getMetaData());

        JsonObject result = toJsonObject(message);

        assertThat(result.getString(ID), is(A_MESSAGE_ID));
        assertThat(result.getString(NAME), is(A_NAME));
        assertThat(result.getString(CORRELATION), is(A_CORRELATION_ID));
        assertThat(result.getString(SESSION_ID), is(A_SESSION_ID));
    }

    @Test
    public void shouldReturnErrorMessageOnError() throws JMSException {
        when(message.getText()).thenThrow(JMSException.class);

        assertThat(toJmsTraceString(message), containsString("Could not find: _metadata in message"));
    }

    private JsonObject toJsonObject(final Message message) {
        StringReader sw = new StringReader(toJmsTraceString(message));
        JsonReader jsonReader = Json.createReader(sw);
        return jsonReader.readObject();
    }

    private String getMetaData() {
        return createObjectBuilder()
                .add("_metadata", createObjectBuilder()
                        .add(ID, A_MESSAGE_ID)
                        .add(NAME, A_NAME)
                        .add(CORRELATION, A_CORRELATION_ID)
                        .add(SESSION_ID, A_SESSION_ID)
                        .add(USER_ID, A_USER_ID)
                        .add(CAUSATION, getCausations()))
                .add("something", "anything really")
                .build().toString();
    }

    private JsonArrayBuilder getCausations() {

        List<UUID> causations = makeCausations();
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (UUID uuid : causations) {
            builder.add(uuid.toString());
        }
        return builder;
    }
}