package uk.gov.justice.services.jmx.command;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mockito.InjectMocks;

import java.lang.reflect.Method;

@RunWith(MockitoJUnitRunner.class)
public class HandlerMethodValidatorTest {
                                                 
    @InjectMocks
    private HandlerMethodValidator handlerMethodValidator;

    @Test
    public void shouldAcceptValidHandlerMethod() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method validHandlerMethod = getMethod("validHandlerMethod", testCommandHandler.getClass());

        handlerMethodValidator.checkHandlerMethodIsValid(validHandlerMethod, testCommandHandler);
    }

    @Test
    public void shouldFailIfMethodNotPublic() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method invalidPrivateHandlerMethod = getMethod("invalidPrivateHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidPrivateHandlerMethod, testCommandHandler);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Handler method 'invalidPrivateHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler' is not public."));
        }
    }

    @Test
    public void shouldFailIfMethodHasNoParameters() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method invalidMissingParameterHandlerMethod = getMethod("invalidMissingParameterHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidMissingParameterHandlerMethod, testCommandHandler);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidMissingParameterHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have one parameter of type 'uk.gov.justice.services.jmx.command.SystemCommand'."));
        }
    }

    @Test
    public void shouldFailIfMethodHasTooManyParameters() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method invalidTooManyParametersHandlerMethod = getMethod("invalidTooManyParametersHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidTooManyParametersHandlerMethod, testCommandHandler);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidTooManyParametersHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have one parameter of type 'uk.gov.justice.services.jmx.command.SystemCommand'."));
        }
    }

    @Test
    public void shouldFailIfMethodDoesNotHaveSystemCommandAsParameter() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method invalidNoSystemCommandHandlerMethod = getMethod("invalidNoSystemCommandHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidNoSystemCommandHandlerMethod, testCommandHandler);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidNoSystemCommandHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have one parameter of type 'uk.gov.justice.services.jmx.command.SystemCommand'."));
        }
    }

    private Method getMethod(final String methodName, final Class<?> handlerClass) {

        for(final Method method: handlerClass.getDeclaredMethods()) {
            if(method.getName().equals(methodName)) {
                return method;
            }
        }

        return null;
    }
}
