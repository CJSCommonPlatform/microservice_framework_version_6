package uk.gov.justice.services.jmx.command;

import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemCommandHandlerProxy {

    private final String commandName;
    private final Method method;
    private final Object instance;
    private final HandlerMethodValidator handlerMethodValidator;

    public SystemCommandHandlerProxy(final String commandName, final Method method, final Object instance, final HandlerMethodValidator handlerMethodValidator) {
        this.commandName = commandName;
        this.method = method;
        this.instance = instance;
        this.handlerMethodValidator = handlerMethodValidator;
    }

    public String getCommandName() {
        return commandName;
    }

    public Object getInstance() {
        return instance;
    }

    public void invokeCommand() {

        handlerMethodValidator.checkHandlerMethodIsValid(method, instance);

        try {
            method.invoke(instance);
        } catch (final IllegalAccessException e) {

            final String message = format("Failed to call method '%s()' on %s. Is the method public?",
                    method.getName(),
                    instance.getClass().getName());
            throw new SystemCommandInvocationException(
                    message,
                    e
            );
        } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            final String message = format("%s thrown when calling method '%s()' on %s",
                    targetException.getClass().getSimpleName(),
                    method.getName(),
                    instance.getClass().getName());
            throw new SystemCommandInvocationException(
                    message,
                    targetException);
        }
    }

}
