package uk.gov.justice.services.management.shuttering.process;

import static java.util.stream.Stream.of;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.jms.EnvelopeSenderSelector;
import uk.gov.justice.services.shuttering.domain.ShutteredCommand;
import uk.gov.justice.services.shuttering.persistence.ShutteringPersistenceException;
import uk.gov.justice.services.shuttering.persistence.ShutteringRepository;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandApiShutteringBeanTest {

    @Mock
    private EnvelopeSenderSelector envelopeSenderSelector;

    @Mock
    private ShutteringRepository shutteringRepository;

    @Mock
    private ShutteredCommandSender shutteredCommandSender;

    @InjectMocks
    private CommandApiShutteringBean commandApiShutteringBean;

    @Test
    public void shouldSetShutteredToTrue() throws Exception {

        commandApiShutteringBean.shutter();

        verify(envelopeSenderSelector).setShuttered(true);
    }

    @Test
    public void shouldDrainTheShutteringCommandQueueAndSetShutteredToFalse() throws Exception {

        final ShutteredCommand shutteredCommand_1 = mock(ShutteredCommand.class);
        final ShutteredCommand shutteredCommand_2 = mock(ShutteredCommand.class);

        when(shutteringRepository.streamShutteredCommands()).thenReturn(of(shutteredCommand_1, shutteredCommand_2));

        commandApiShutteringBean.unshutter();

        final InOrder inOrder = inOrder(shutteredCommandSender, envelopeSenderSelector);

        inOrder.verify(shutteredCommandSender).sendAndDelete(shutteredCommand_1);
        inOrder.verify(shutteredCommandSender).sendAndDelete(shutteredCommand_2);
        inOrder.verify(envelopeSenderSelector).setShuttered(false);
    }

    @Test
    public void shouldNotUnshutterIfDrainingTheCommandQueueFails() throws Exception {

        final ShutteringPersistenceException shutteringPersistenceException = new ShutteringPersistenceException("Ooops");

        final ShutteredCommand shutteredCommand_1 = mock(ShutteredCommand.class);
        final ShutteredCommand shutteredCommand_2 = mock(ShutteredCommand.class);

        when(shutteringRepository.streamShutteredCommands()).thenReturn(of(shutteredCommand_1, shutteredCommand_2));
        Mockito.doThrow(shutteringPersistenceException).when(shutteredCommandSender).sendAndDelete(shutteredCommand_2);

        try {
            commandApiShutteringBean.unshutter();
            fail();
        } catch (final ShutteringPersistenceException expected) {
            assertThat(expected, CoreMatchers.is(shutteringPersistenceException));
        }

        verify(shutteredCommandSender).sendAndDelete(shutteredCommand_1);

        verifyZeroInteractions(envelopeSenderSelector);
    }
}
