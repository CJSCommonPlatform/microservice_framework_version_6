package uk.gov.justice.services.core.handler.registry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.registry.exception.DuplicateHandlerException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;

import static java.util.Arrays.stream;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

/**
 * Unit tests for the HandlerRegistry class.
 */
@RunWith(MockitoJUnitRunner.class)
public class HandlerRegistryTest {

    private static final String COMMAND_NAME = "test.commands.mock-command";
    private static final String COMMAND_NAME_OTHER = "test.commands.mock-command-other";
    private static final String EXPECTED_NO_METHOD_ANNOTATION_EXCEPTION_MESSAGE = "Class uk.gov.justice.services.core.handler.registry.HandlerRegistryTest$CommandHandlerWithNoAnnotation doesn't have any method annotated with the annotation @Handles";
    private static final String EXPECTED_DUPLICATE_HANDLER_MESSAGE = "Can not register HandlerInstanceAndMethod[ Class:uk.gov.justice.services.core.handler.registry.HandlerRegistryTest$CommandHandlerDuplicate method:handle], because a command handler method HandlerInstanceAndMethod[ Class:uk.gov.justice.services.core.handler.registry.HandlerRegistryTest$CommandHandler method:handle] has already been registered for test.commands.mock-command";
    private static final String EXPECTED_ENVELOPE_AS_ARGUMENT_MESSAGE = "Handler methods must receive Envelope as an argument.";
    private static final String EXPECTED_ONE_PARAMETER_MESSAGE = "Handles method must have exactly one parameter. Found ";

    @Mock
    private CommandHandler commandHandler;

    @Mock
    private CommandHandlerDuplicate commandHandlerDuplicate;

    private HandlerRegistry registry;

    @Test
    public void shouldHandleActionWithAValidHandler() {
        createRegistryFor(new CommandHandler());
        assertThat(registry.canHandle(COMMAND_NAME), equalTo(true));
    }

    @Test
    public void shouldReturnTheCorrectHandler() {
        createRegistryFor(new CommandHandler());
        assertThat(registry.get(COMMAND_NAME), notNullValue());
    }

    @Test
    public void shouldHandleMultipleMethodAnnotations() throws Exception {
        createRegistryFor(new CommandHandlerWithMultipleAnnotations());
        assertThat(registry.canHandle(COMMAND_NAME), equalTo(true));
        assertThat(registry.canHandle(COMMAND_NAME_OTHER), equalTo(true));
    }

    @Test
    public void shouldReturnTheCorrectHandlers() throws Exception {
        createRegistryFor(new CommandHandlerWithMultipleAnnotations());
        assertThat(registry.get(COMMAND_NAME), notNullValue());
        assertThat(registry.get(COMMAND_NAME_OTHER), notNullValue());
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfNoMethodAnnotation() {
        exception.expect(InvalidHandlerException.class);
        exception.expectMessage(EXPECTED_NO_METHOD_ANNOTATION_EXCEPTION_MESSAGE);

        createRegistryFor(new CommandHandlerWithNoAnnotation());
    }

    @Test
    public void shouldThrowExceptionWithMultipleArgumentsHandler() {
        exception.expect(InvalidHandlerException.class);
        exception.expectMessage(EXPECTED_ONE_PARAMETER_MESSAGE);

        createRegistryFor(new CommandHandlerWithWrongHandler());
    }

    @Test
    public void shouldThrowExceptionWithDuplicateHandlers() {
        exception.expect(DuplicateHandlerException.class);
        exception.expectMessage(EXPECTED_DUPLICATE_HANDLER_MESSAGE);

        createRegistryFor(new CommandHandler(), new CommandHandlerDuplicate());
    }

    @Test
    public void shouldThrowExceptionWithWrongParameters() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(EXPECTED_ENVELOPE_AS_ARGUMENT_MESSAGE);

        createRegistryFor(new CommandHandler(), new CommandHandlerWithWrongParameter());
    }

    private void createRegistryFor(Object... handlers) {
        registry = new HandlerRegistry();
        stream(handlers).forEach(registry::register);
    }


    @ServiceComponent(COMMAND_HANDLER)
    public static class CommandHandler {

        @Handles(COMMAND_NAME)
        public void handle(Envelope envelope) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class CommandHandlerWithMultipleAnnotations {

        @Handles(COMMAND_NAME)
        public void handleCommand(Envelope envelope) {
        }

        @Handles(COMMAND_NAME_OTHER)
        public void handleCommandOther(Envelope envelope) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class CommandHandlerDuplicate {

        @Handles(COMMAND_NAME)
        public void handle(Envelope envelope) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class CommandHandlerWithWrongHandler {

        @Handles(COMMAND_NAME)
        public void handle1(Envelope envelope, Object invalidSecondArgument) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class CommandHandlerWithWrongParameter {

        @Handles(COMMAND_NAME)
        public void handle1(Object invalidSecondArgument) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class CommandHandlerWithNoAnnotation {

        public void nonHandlerMethod(String command) {
        }
    }

}
