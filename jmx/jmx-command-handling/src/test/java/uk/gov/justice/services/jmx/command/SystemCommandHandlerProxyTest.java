package uk.gov.justice.services.jmx.command;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.jmx.command.TestCommand.TEST_COMMAND;

import uk.gov.justice.services.jmx.api.SystemCommandInvocationException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.Test;

public class SystemCommandHandlerProxyTest {

    @Test
    public void shouldInvokeTheCommandHandlerMethod() throws Exception {

        final UUID commandId = UUID.randomUUID();
        final TestCommand testCommand = new TestCommand();
        final DummyHandler dummyHandler = new DummyHandler();
        final Method method = getMethod("someHandlerMethod", dummyHandler);

        final HandlerMethodValidator handlerMethodValidator = mock(HandlerMethodValidator.class);

        final SystemCommandHandlerProxy systemCommandHandlerProxy = new SystemCommandHandlerProxy(
                testCommand.getName(),
                method,
                dummyHandler,
                handlerMethodValidator);

        assertThat(systemCommandHandlerProxy.getCommandName(), is(testCommand.getName()));
        assertThat(systemCommandHandlerProxy.getInstance(), is(dummyHandler));

        assertThat(dummyHandler.someHandlerMethodWasCalled(), is(false));

        systemCommandHandlerProxy.invokeCommand(testCommand, commandId);

        assertThat(dummyHandler.someHandlerMethodWasCalled(), is(true));

        verify(handlerMethodValidator).checkHandlerMethodIsValid(method, dummyHandler);
    }

    @Test
    public void shouldFailIfTheMethodIsInaccessible() throws Exception {

        final UUID commandId = UUID.randomUUID();
        final TestCommand testCommand = new TestCommand();
        final DummyHandler dummyHandler = new DummyHandler();
        final Method method = getMethod("aPrivateMethod", dummyHandler);

        final HandlerMethodValidator handlerMethodValidator = mock(HandlerMethodValidator.class);

        final SystemCommandHandlerProxy systemCommandHandlerProxy = new SystemCommandHandlerProxy(
                testCommand.getName(),
                method,
                dummyHandler,
                handlerMethodValidator);

        try {
            systemCommandHandlerProxy.invokeCommand(testCommand, commandId);
            fail();
        } catch (final SystemCommandInvocationException expected) {
            assertThat(expected.getMessage(), is("Failed to call method 'aPrivateMethod()' on " + dummyHandler.getClass().getName() + ". Is the method public?"));
            assertThat(expected.getCause(), is(instanceOf(IllegalAccessException.class)));
        }

        verify(handlerMethodValidator).checkHandlerMethodIsValid(method, dummyHandler);
    }

    @Test
    public void shouldFailIfTheInvokedMethodThrowsAnException() throws Exception {

        final UUID commandId = UUID.randomUUID();
        final TestCommand testCommand = new TestCommand();
        final DummyHandler dummyHandler = new DummyHandler();
        final Method method = getMethod("anExceptionThrowingMethod", dummyHandler);

        final HandlerMethodValidator handlerMethodValidator = mock(HandlerMethodValidator.class);

        final SystemCommandHandlerProxy systemCommandHandlerProxy = new SystemCommandHandlerProxy(
                testCommand.getName(),
                method,
                dummyHandler,
                handlerMethodValidator);

        try {
            systemCommandHandlerProxy.invokeCommand(testCommand, commandId);
            fail();
        } catch (final SystemCommandInvocationException expected) {
            assertThat(expected.getMessage(), is("IOException thrown when calling method 'anExceptionThrowingMethod()' on " + dummyHandler.getClass().getName()));
            assertThat(expected.getCause(), is(instanceOf(IOException.class)));
        }

        verify(handlerMethodValidator).checkHandlerMethodIsValid(method, dummyHandler);
    }

    private Method getMethod(final String methodName, final Object instance) {

        for(final Method method: instance.getClass().getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }

        return null;
    }

    private class DummyHandler {

        private boolean someHandlerMethodCalled = false;

        @HandlesSystemCommand(TEST_COMMAND)
        public void someHandlerMethod(final TestCommand testCommand, final UUID commandId) {
              someHandlerMethodCalled = true;
        }

        @HandlesSystemCommand("PRIVATE_TEST_COMMAND")
        private void aPrivateMethod(final SystemCommand systemCommand, final UUID commandId) {}

        @HandlesSystemCommand("DODGY_TEST_COMMAND")
        public void anExceptionThrowingMethod(final SystemCommand systemCommand, final UUID commandId) throws IOException {
            throw new IOException("Ooops");
        }

        public boolean someHandlerMethodWasCalled() {
            return someHandlerMethodCalled;
        }
    }
}
