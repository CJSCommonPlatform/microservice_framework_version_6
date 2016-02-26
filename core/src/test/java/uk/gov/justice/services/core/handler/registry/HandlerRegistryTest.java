package uk.gov.justice.services.core.handler.registry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.registry.exception.DuplicateHandlerException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;

import java.util.Arrays;

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

    @Mock
    private TestCommandHandler commandHandler;

    @Mock
    private TestCommandHandlerDuplicate commandHandlerDuplicate;

    private HandlerRegistry registry;

    @Test
    public void shouldHandleActionWithAValidHandler() {
        createRegistry(new TestCommandHandler());
        assertThat(registry.canHandle(COMMAND_NAME), equalTo(true));
    }

    @Test
    public void shouldReturnTheCorrectHandler() {
        createRegistry(new TestCommandHandler());
        assertThat(registry.get(COMMAND_NAME), notNullValue());
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithMultipleArgumentsHandler() {
        createRegistry(new TestCommandHandlerWithWrongHandler());
    }

    @Test(expected = DuplicateHandlerException.class)
    public void shouldThrowExceptionWithDuplicateHandlers() {
        createRegistry(new TestCommandHandler(), new TestCommandHandlerDuplicate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithWrongParameters() {
        createRegistry(new TestCommandHandler(), new TestCommandHandlerWithWrongParameter());
    }

    private void createRegistry(Object... handlers) {
        registry = new HandlerRegistry();
        Arrays.asList(handlers).stream().forEach(x -> registry.register(x));
    }

    public static class MockCommand {
    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandler {

        @Handles(COMMAND_NAME)
        public void handle(Envelope envelope) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerDuplicate {

        @Handles(COMMAND_NAME)
        public void handle(Envelope envelope) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithWrongHandler {

        @Handles(COMMAND_NAME)
        public void handle1(Envelope envelope, Object invalidSecondArgument) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithWrongParameter {

        @Handles(COMMAND_NAME)
        public void handle1(Object invalidSecondArgument) {
        }

    }
}
