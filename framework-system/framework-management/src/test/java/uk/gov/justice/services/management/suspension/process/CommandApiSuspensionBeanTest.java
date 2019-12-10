package uk.gov.justice.services.management.suspension.process;

import static java.util.stream.Stream.of;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.polling.MultiIteratingPollerFactory;
import uk.gov.justice.services.management.suspension.executors.CommandApiSuspensionBean;
import uk.gov.justice.services.management.suspension.executors.StoredCommandSender;
import uk.gov.justice.services.messaging.jms.EnvelopeSenderSelector;
import uk.gov.justice.services.system.domain.StoredCommand;
import uk.gov.justice.services.system.persistence.StoredCommandRepository;
import uk.gov.justice.services.test.utils.common.polling.DummyMultiIteratingPoller;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandApiSuspensionBeanTest {

    @Mock
    private EnvelopeSenderSelector envelopeSenderSelector;

    @Mock
    private StoredCommandRepository storedCommandRepository;

    @Mock
    private StoredCommandSender storedCommandSender;

    @Mock
    private MultiIteratingPollerFactory multiIteratingPollerFactory;

    @InjectMocks
    private CommandApiSuspensionBean commandApiSuspensionBean;

    @Test
    public void shouldSetSuspendedToTrue() throws Exception {

        commandApiSuspensionBean.suspend();

        verify(envelopeSenderSelector).setSuspended(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDrainTheShutteringCommandQueueAndSetShutteredToFalse() throws Exception {

        final StoredCommand storedCommand_1 = mock(StoredCommand.class);
        final StoredCommand storedCommand_2 = mock(StoredCommand.class);
        final StoredCommand storedCommand_3 = mock(StoredCommand.class);
        final StoredCommand storedCommand_4 = mock(StoredCommand.class);

        final Stream<StoredCommand> shutteredCommands_1 = of(storedCommand_1, storedCommand_2);
        final Stream<StoredCommand> shutteredCommands_2 = of(storedCommand_3, storedCommand_4);

        when(storedCommandRepository.streamStoredCommands()).thenReturn(shutteredCommands_1, shutteredCommands_2);
        when(multiIteratingPollerFactory.create(2, 100L, 2, 100L)).thenReturn(new DummyMultiIteratingPoller());

        commandApiSuspensionBean.unsuspend();

        final InOrder inOrder = inOrder(storedCommandSender, envelopeSenderSelector);

        inOrder.verify(storedCommandSender).sendAndDelete(storedCommand_1);
        inOrder.verify(storedCommandSender).sendAndDelete(storedCommand_2);
        inOrder.verify(envelopeSenderSelector).setSuspended(false);
        inOrder.verify(storedCommandSender).sendAndDelete(storedCommand_3);
        inOrder.verify(storedCommandSender).sendAndDelete(storedCommand_4);
    }


}
