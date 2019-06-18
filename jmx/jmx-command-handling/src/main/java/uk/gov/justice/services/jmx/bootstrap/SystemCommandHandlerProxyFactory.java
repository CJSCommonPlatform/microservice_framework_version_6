package uk.gov.justice.services.jmx.bootstrap;

import uk.gov.justice.services.jmx.command.HandlerMethodValidator;
import uk.gov.justice.services.jmx.command.SystemCommandHandlerProxy;

import java.lang.reflect.Method;

public class SystemCommandHandlerProxyFactory {

    public SystemCommandHandlerProxy create(final String commandName, final Method method, final Object instance, final HandlerMethodValidator handlerMethodValidator) {
        return new SystemCommandHandlerProxy(commandName, method, instance, handlerMethodValidator);
    }
}
