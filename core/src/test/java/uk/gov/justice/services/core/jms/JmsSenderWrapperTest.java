package uk.gov.justice.services.core.jms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;

import javax.json.Json;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JmsSenderWrapperTest {

    private static final String TEST_FIELD = "id";
    private static final String TEST_UUID = UUID.randomUUID().toString();
    private static final String TEST_VALUE = "test";

    @Mock
    Sender primarySender;

    @Mock
    Sender legacySender;

    @Test
    public void primarySenderShouldCorrectlySendRequest() {
        JsonEnvelope requestPayload = generateStubRequest();

        JmsSenderWrapper wrapper = new JmsSenderWrapper(primarySender, null);
        wrapper.send(requestPayload);

        verify(primarySender, times(1)).send(requestPayload);
    }

    @Test
    public void shouldDelegateToLegacySender() {
        doThrow(new MissingHandlerException("Primary sender is undefined")).when(primarySender).send(any());
        JsonEnvelope requestPayload = generateStubRequest();

        JmsSenderWrapper wrapper = new JmsSenderWrapper(primarySender, legacySender);
        wrapper.send(requestPayload);

        verify(legacySender, times(1)).send(requestPayload);
    }

    @Test
    public void shouldThrowDetailedExceptionIfPrimarySenderUndefined() {
        JmsSenderWrapper wrapper = new JmsSenderWrapper(null, null);
        wrapper.send(generateStubRequest());
    }

    @Test
    public void shouldThrowDetailedExceptionIfLegacySenderUndefined() {
        doThrow(new MissingHandlerException("Primary sender is undefined")).when(primarySender).send(any());

        JmsSenderWrapper wrapper = new JmsSenderWrapper(primarySender, null);
        wrapper.send(generateStubRequest());
    }

    private JsonEnvelope generateStubRequest() {
        return DefaultJsonEnvelope.envelopeFrom(
                JsonObjectMetadata.metadataOf(TEST_UUID, TEST_VALUE).build(),
                Json.createObjectBuilder().add(TEST_FIELD, TEST_VALUE).build()
        );
    }
}

