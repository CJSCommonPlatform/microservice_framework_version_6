package uk.gov.justice.services.management.shuttering.lifecycle;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsSender;
import uk.gov.justice.services.shuttering.domain.ShutteredCommand;
import uk.gov.justice.services.shuttering.persistence.ShutteringRepository;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShutteredCommandSenderTest {

    @Mock
    private ShutteringRepository shutteringRepository;

    @Mock
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Mock
    private JmsSender jmsSender;

    @InjectMocks
    private ShutteredCommandSender shutteredCommandSender;

    @Test
    public void shouldSendAndDeleteShutteredCommand() throws Exception {

        final UUID envelopeId = randomUUID();
        final String destination = "destination";
        final String commandJsonEnvelope = "command envelope";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        final ZonedDateTime now = new UtcClock().now();

        final ShutteredCommand shutteredCommand = new ShutteredCommand(
                envelopeId,
                commandJsonEnvelope,
                destination,
                now);

        when(jsonObjectEnvelopeConverter.asEnvelope(commandJsonEnvelope)).thenReturn(jsonEnvelope);

        shutteredCommandSender.sendAndDelete(shutteredCommand);

        final InOrder inOrder = inOrder(jmsSender, shutteringRepository);

        inOrder.verify(jmsSender).send(jsonEnvelope, destination);
        inOrder.verify(shutteringRepository).delete(envelopeId);
    }
}
