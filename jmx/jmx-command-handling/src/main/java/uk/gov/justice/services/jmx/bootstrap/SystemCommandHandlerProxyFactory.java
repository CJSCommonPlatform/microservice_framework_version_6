package uk.gov.justice.services.jmx.bootstrap;

import uk.gov.justice.services.jmx.command.SystemCommandHandlerProxy;

import java.lang.reflect.Method;

public class SystemCommandHandlerProxyFactory {

    public SystemCommandHandlerProxy create(final String commandName, final Method method, final Object instance) {
        return new SystemCommandHandlerProxy(commandName, method, instance);
    }
}
