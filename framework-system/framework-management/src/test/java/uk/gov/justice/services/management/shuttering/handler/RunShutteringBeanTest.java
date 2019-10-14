package uk.gov.justice.services.management.shuttering.handler;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.jmx.api.command.ApplicationShutteringCommand;
import uk.gov.justice.services.management.shuttering.process.ShutteringProcessRunner;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RunShutteringBeanTest {

    @Mock
    private ShutteringProcessRunner shutteringProcessRunner;

    @InjectMocks
    private RunShutteringBean runShutteringBean;

    @Test
    public void shouldRunShuttering() throws Exception {

        final UUID commandId = randomUUID();
        final ApplicationShutteringCommand applicationShutteringCommand = mock(ApplicationShutteringCommand.class);

        runShutteringBean.runShuttering(commandId, applicationShutteringCommand);

        verify(shutteringProcessRunner).runShuttering(commandId, applicationShutteringCommand);
    }
}
