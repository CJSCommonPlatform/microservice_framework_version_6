package uk.gov.justice.services.jmx.command;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;

import uk.gov.justice.services.jmx.api.InvalidHandlerMethodException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.lang.reflect.Method;
import java.util.UUID;

public class HandlerMethodValidator {


    public void checkHandlerMethodIsValid(final Method handlerMethod, final Object instance) {

        checkMethodPublic(handlerMethod, instance);
        checkMethodParameter(handlerMethod, instance);
    }

    private void checkMethodPublic(final Method handlerMethod, final Object instance) {

        if (!isPublic(handlerMethod.getModifiers())) {
            throw new InvalidHandlerMethodException(format("Handler method '%s' on class '%s' is not public.", handlerMethod.getName(), instance.getClass().getName()));
        }
    }

    private void checkMethodParameter(final Method handlerMethod, final Object instance) {
        final Class<?>[] parameterTypes = handlerMethod.getParameterTypes();

        if (parameterTypes.length != 2) {
            throw new InvalidHandlerMethodException(format("Invalid handler method '%s' on class '%s'. Method should have 2 parameters. First of type '%s' and second of type '%s'.",
                    handlerMethod.getName(),
                    instance.getClass().getName(),
                    SystemCommand.class.getName(),
                    UUID.class.getName()));
        }

        if (!SystemCommand.class.isAssignableFrom(parameterTypes[0])) {
            throw new InvalidHandlerMethodException(format("Invalid handler method '%s' on class '%s'. Method should have one parameter of type '%s'.",
                    handlerMethod.getName(),
                    instance.getClass().getName(),
                    SystemCommand.class.getName()));
        }
    }
}
