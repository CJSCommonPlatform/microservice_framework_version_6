package uk.gov.justice.services.jmx.command;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;

public class HandlesSystemCommandHandlerProxyTest {

    @Test
    public void shouldInvokeTheCommandHandlerMethod() throws Exception {

        final String commandName = "SOME_COMMAND";
        final DummyHandler dummyHandler = new DummyHandler();
        final Method method = dummyHandler.getClass().getDeclaredMethod("someHandlerMethod");

        final SystemCommandHandlerProxy systemCommandHandlerProxy = new SystemCommandHandlerProxy(commandName, method, dummyHandler);

        assertThat(systemCommandHandlerProxy.getCommandName(), is(commandName));
        assertThat(systemCommandHandlerProxy.getInstance(), is(dummyHandler));

        assertThat(dummyHandler.someHandlerMethodWasCalled(), is(false));

        systemCommandHandlerProxy.invokeCommand();

        assertThat(dummyHandler.someHandlerMethodWasCalled(), is(true));
    }

    @Test
    public void shouldFailIfTheMethodIsInaccessible() throws Exception {

        final String commandName = "SOME_COMMAND";
        final DummyHandler dummyHandler = new DummyHandler();
        final Method method = dummyHandler.getClass().getDeclaredMethod("aPrivateMethod");

        final SystemCommandHandlerProxy systemCommandHandlerProxy = new SystemCommandHandlerProxy(commandName, method, dummyHandler);

        try {
            systemCommandHandlerProxy.invokeCommand();
            fail();
        } catch (final SystemCommandInvocationException expected) {
            assertThat(expected.getMessage(), is("Failed to call method 'aPrivateMethod()' on " + dummyHandler.getClass().getName() + ". Is the method public?"));
            assertThat(expected.getCause(), is(instanceOf(IllegalAccessException.class)));
        }
    }

    @Test
    public void shouldFailIfTheInvokedMethodThrowsAnException() throws Exception {

        final String commandName = "SOME_COMMAND";
        final DummyHandler dummyHandler = new DummyHandler();
        final Method method = dummyHandler.getClass().getDeclaredMethod("anExceptionThrowingMethod");

        final SystemCommandHandlerProxy systemCommandHandlerProxy = new SystemCommandHandlerProxy(commandName, method, dummyHandler);

        try {
            systemCommandHandlerProxy.invokeCommand();
            fail();
        } catch (final SystemCommandInvocationException expected) {
            assertThat(expected.getMessage(), is("IOException thrown when calling method 'anExceptionThrowingMethod()' on " + dummyHandler.getClass().getName()));
            assertThat(expected.getCause(), is(instanceOf(IOException.class)));
        }
    }

    private class DummyHandler {

        private boolean someHandlerMethodCalled = false;

        public void someHandlerMethod() {
              someHandlerMethodCalled = true;
        }

        private void aPrivateMethod() {}

        public void anExceptionThrowingMethod() throws IOException {
            throw new IOException("Ooops");
        }

        public boolean someHandlerMethodWasCalled() {
            return someHandlerMethodCalled;
        }
    }
}
