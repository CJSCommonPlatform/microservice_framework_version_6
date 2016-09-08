package uk.gov.justice.services.core.jms;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmsSenderWrapperTest {

    @Mock
    Sender primarySender;

    @Mock
    Sender legacySender;

    @Test
    public void primarySenderShouldCorrectlySendRequest() {
        JsonEnvelope requestEnvelope = envelope().build();

        JmsSenderWrapper wrapper = new JmsSenderWrapper(primarySender, null);
        wrapper.send(requestEnvelope);

        verify(primarySender, times(1)).send(requestEnvelope);
    }

    @Test
    public void shouldDelegateToLegacySender() {
        doThrow(new MissingHandlerException("Handler not found")).when(primarySender).send(any());
        JsonEnvelope requestEnvelope = envelope().build();

        JmsSenderWrapper wrapper = new JmsSenderWrapper(primarySender, Optional.of(legacySender));
        wrapper.send(requestEnvelope);

        verify(legacySender, times(1)).send(requestEnvelope);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldRethrowMissingHandlerExceptionIfSecondaryHandlerNotProvided() {
        doThrow(new MissingHandlerException("Handler not found")).when(primarySender).send(any());

        exception.expect(MissingHandlerException.class);
        exception.expectMessage("Handler not found");

        JmsSenderWrapper wrapper = new JmsSenderWrapper(primarySender, Optional.empty());
        wrapper.send(envelope().build());
    }

}