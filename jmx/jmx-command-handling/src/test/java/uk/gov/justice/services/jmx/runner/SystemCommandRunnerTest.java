package uk.gov.justice.services.jmx.runner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.framework.utilities.exceptions.StackTraceProvider;
import uk.gov.justice.services.jmx.api.SystemCommandException;
import uk.gov.justice.services.jmx.api.SystemCommandFailedException;
import uk.gov.justice.services.jmx.command.SystemCommandHandlerProxy;
import uk.gov.justice.services.jmx.command.SystemCommandStore;
import uk.gov.justice.services.jmx.command.TestCommand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class SystemCommandRunnerTest {

    @Mock
    private SystemCommandStore systemCommandStore;

    @Mock
    private StackTraceProvider stackTraceProvider;

    @Mock
    private Logger logger;

    @InjectMocks
    private SystemCommandRunner systemCommandRunner;

    @Test
    public void shouldReturnFalseIfCommandUnsupported() throws Exception {

        final TestCommand testCommand = new TestCommand();
        final boolean supported = true;

        when(systemCommandStore.isSupported(testCommand)).thenReturn(supported);

        assertThat(systemCommandRunner.isSupported(testCommand), is(supported));
    }

    @Test
    public void shouldFindTheCorrectProxyForTheCommandAndInvoke() throws Exception {

        final TestCommand testCommand = new TestCommand();

        final SystemCommandHandlerProxy systemCommandHandlerProxy = mock(SystemCommandHandlerProxy.class);

        when(systemCommandStore.findCommandProxy(testCommand)).thenReturn(systemCommandHandlerProxy);

        systemCommandRunner.run(testCommand);

        verify(systemCommandHandlerProxy).invokeCommand(testCommand);
    }

    @Test
    public void shouldThrowSystemCommandFailedExceptionIfCommandFails() throws Exception {

        final TestCommand testCommand = new TestCommand();
        final SystemCommandException systemCommandException = new SystemCommandException("Ooops");
        final String stackTrace = "stack trace";

        final SystemCommandHandlerProxy systemCommandHandlerProxy = mock(SystemCommandHandlerProxy.class);

        when(systemCommandStore.findCommandProxy(testCommand)).thenReturn(systemCommandHandlerProxy);
        doThrow(systemCommandException).when(systemCommandHandlerProxy).invokeCommand(testCommand);
        when(stackTraceProvider.getStackTrace(systemCommandException)).thenReturn(stackTrace);

        try {
            systemCommandRunner.run(testCommand);
            fail();
        } catch (final SystemCommandFailedException expected) {
            assertThat(expected.getMessage(), is("Failed to run System Command 'TEST_COMMAND'. Caused by uk.gov.justice.services.jmx.api.SystemCommandException: Ooops"));
            assertThat(expected.getServerStackTrace(), is(stackTrace));
            assertThat(expected.getCause(), is(nullValue()));
        }
    }
}
