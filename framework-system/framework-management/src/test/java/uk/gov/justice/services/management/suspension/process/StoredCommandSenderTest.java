package uk.gov.justice.services.management.suspension.process;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.management.suspension.executors.StoredCommandSender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsSender;
import uk.gov.justice.services.shuttering.domain.StoredCommand;
import uk.gov.justice.services.shuttering.persistence.StoredCommandRepository;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StoredCommandSenderTest {

    @Mock
    private StoredCommandRepository storedCommandRepository;

    @Mock
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Mock
    private JmsSender jmsSender;

    @InjectMocks
    private StoredCommandSender storedCommandSender;

    @Test
    public void shouldSendAndDeleteShutteredCommand() throws Exception {

        final UUID envelopeId = randomUUID();
        final String destination = "destination";
        final String commandJsonEnvelope = "command envelope";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        final ZonedDateTime now = new UtcClock().now();

        final StoredCommand storedCommand = new StoredCommand(
                envelopeId,
                commandJsonEnvelope,
                destination,
                now);

        when(jsonObjectEnvelopeConverter.asEnvelope(commandJsonEnvelope)).thenReturn(jsonEnvelope);

        storedCommandSender.sendAndDelete(storedCommand);

        final InOrder inOrder = inOrder(jmsSender, storedCommandRepository);

        inOrder.verify(jmsSender).send(jsonEnvelope, destination);
        inOrder.verify(storedCommandRepository).delete(envelopeId);
    }
}
