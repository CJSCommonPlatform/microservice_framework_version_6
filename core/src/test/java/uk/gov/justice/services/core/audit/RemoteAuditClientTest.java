package uk.gov.justice.services.core.audit;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.util.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class RemoteAuditClientTest {

    @Mock
    private Logger logger;

    @Mock
    private Requester requester;

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Mock
    private ServiceContextNameProvider serviceContextNameProvider;

    @InjectMocks
    private RemoteAuditClient remoteAuditClient;

    @Test
    public void shouldVerifyAuditMessageSent() throws Exception {

        final String contextName = "context-command-api";
        final String message = "a message";
        final String timestamp = ZonedDateTimes.toString(now());

        final UUID id = randomUUID();

        final JsonEnvelope envelope = envelope()
                .with(metadataOf(id, "some.action"))
                .withPayloadOf(timestamp, "timestamp")
                .withPayloadOf(contextName, "origin")
                .withPayloadOf(message, "message")
                .build();

        when(serviceContextNameProvider.getServiceContextName()).thenReturn(contextName);

        remoteAuditClient.auditEntry(envelope);

        final ArgumentCaptor<JsonEnvelope> argumentCaptor = forClass(JsonEnvelope.class);

        verify(requester).request(argumentCaptor.capture());

        final JsonEnvelope actualEnvelope = argumentCaptor.getValue();

        with(actualEnvelope.metadata().asJsonObject().toString())
                .assertEquals("name", "audit.record-entry")
                .assertNotNull("id")
                .assertEquals("causation[0]", id.toString());

        with(actualEnvelope.payload().toString())
                .assertEquals("origin", contextName)
                .assertNotNull("timestamp")
                .assertEquals("message", message);
    }

    @Test
    public void shouldThrowException() throws Exception {

        final UUID id = randomUUID();
        final String contextName = "context-command-api";
        final String message = "a message";
        final String timestamp = ZonedDateTimes.toString(now());

        final String metadataName = "some.action";

        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Ooops");

        final JsonEnvelope envelope = envelope()
                .with(metadataOf(id, metadataName))
                .withPayloadOf(timestamp, "timestamp")
                .withPayloadOf(contextName, "origin")
                .withPayloadOf(message, "message")
                .build();

        when(serviceContextNameProvider.getServiceContextName()).thenReturn(contextName);
        when(requester.request(any(JsonEnvelope.class))).thenThrow(illegalArgumentException);

        remoteAuditClient.auditEntry(envelope);

        final ArgumentCaptor<String> argumentCaptor = forClass(String.class);

        verify(logger).error(argumentCaptor.capture(), eq(illegalArgumentException));

        final String errorMessage = argumentCaptor.getValue();

        final String messagePrefix = "Failed to audit entry for ";
        assertThat(errorMessage, startsWith(messagePrefix));

        final String json = errorMessage.substring(messagePrefix.length());

        with(json)
                .assertEquals("metadata.id", id.toString())
                .assertEquals("metadata.name", metadataName)
                .assertEquals("payload.timestamp", timestamp)
                .assertEquals("payload.origin", contextName)
                .assertEquals("payload.message", message);
    }
}
