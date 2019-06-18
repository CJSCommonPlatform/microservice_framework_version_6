package uk.gov.justice.services.jmx.bootstrap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.jmx.command.HandlerMethodValidator;
import uk.gov.justice.services.jmx.command.SystemCommandHandlerProxy;

import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class HandlesSystemCommandHandlerProxyFactoryTest {


    @InjectMocks
    private SystemCommandHandlerProxyFactory systemCommandHandlerProxyFactory;

    @Test
    public void shouldCreateSystemCommandHandlerProxy() throws Exception {

        final String commandName = "command name";
        final Method method = AnotherHandlesSystemCommandHandler.class.getDeclaredMethod("aCommandMethod");
        final Object instance = new AnotherHandlesSystemCommandHandler();

        final HandlerMethodValidator handlerMethodValidator = mock(HandlerMethodValidator.class);


        final SystemCommandHandlerProxy systemCommandHandlerProxy = systemCommandHandlerProxyFactory.create(commandName, method, instance, handlerMethodValidator);

        assertThat(getValueOfField(systemCommandHandlerProxy, "commandName", String.class), is(commandName));
        assertThat(getValueOfField(systemCommandHandlerProxy, "method", Method.class), is(method));
        assertThat(getValueOfField(systemCommandHandlerProxy, "instance", Object.class), is(instance));
        assertThat(getValueOfField(systemCommandHandlerProxy, "handlerMethodValidator", HandlerMethodValidator.class), is(handlerMethodValidator));
    }

    private class AnotherHandlesSystemCommandHandler {

        public void aCommandMethod() {

        }
    }
}
