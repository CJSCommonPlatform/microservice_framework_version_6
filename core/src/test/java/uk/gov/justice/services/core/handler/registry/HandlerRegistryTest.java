package uk.gov.justice.services.core.handler.registry;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.registry.exception.DuplicateHandlerException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    public void shouldHandleActionWithAValidAsynchronousHandler() {
        createRegistry(new TestCommandHandler());
        assertThat(registry.canHandleAsynchronous(COMMAND_NAME), equalTo(true));
        assertThat(registry.canHandleSynchronous(COMMAND_NAME), equalTo(false));
    }

    @Test
    public void shouldHandleActionWithAValidSynchronousHandler() {
        createRegistry(new TestCommandHandlerWithSynchronousHandler());
        assertThat(registry.canHandleSynchronous(COMMAND_NAME), equalTo(true));
        assertThat(registry.canHandleAsynchronous(COMMAND_NAME), equalTo(false));
    }

    @Test
    public void shouldReturnTheRegisteredAsynchronousHandler() {
        createRegistry(new TestCommandHandler());
        assertThat(registry.getAsynchronous(COMMAND_NAME), notNullValue());
    }

    @Test
    public void shouldReturnTheRegisteredSynchronousHandler() {
        createRegistry(new TestCommandHandlerWithSynchronousHandler());
        assertThat(registry.getSynchronous(COMMAND_NAME), notNullValue());
    }

    @Test
    public void shouldNotReturnTheRegisteredAsynchronousHandler() {
        createRegistry(new TestCommandHandler());
        assertThat(registry.getSynchronous(COMMAND_NAME), nullValue());
    }

    @Test
    public void shouldNotReturnTheRegisteredSynchronousHandler() {
        createRegistry(new TestCommandHandlerWithSynchronousHandler());
        assertThat(registry.getAsynchronous(COMMAND_NAME), nullValue());
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithMultipleArgumentsAsynchronousHandler() {
        createRegistry(new TestCommandHandlerWithWrongHandler());
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithMultipleArgumentsSynchronousHandler() {
        createRegistry(new TestCommandHandlerWithWrongSynchronousHandler());
    }

    @Test(expected = DuplicateHandlerException.class)
    public void shouldThrowExceptionWithDuplicateAsynchronousHandlers() {
        createRegistry(new TestCommandHandler(), new TestCommandHandlerDuplicate());
    }

    @Test(expected = DuplicateHandlerException.class)
    public void shouldThrowExceptionWithDuplicateSynchronousHandlers() {
        createRegistry(new TestCommandHandlerWithSynchronousHandler(), new TestCommandHandlerWithSynchronousHandlerDuplicate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithAsynchronousWrongParameters() {
        createRegistry(new TestCommandHandler(), new TestCommandHandlerWithWrongParameter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithSynchronousWrongParameters() {
        createRegistry(new TestCommandHandlerWithSynchronousHandler(), new TestCommandSynchronousHandlerWithWrongParameter());
    }

    private void createRegistry(Object... handlers) {
        registry = new HandlerRegistry();
        asList(handlers).stream().forEach(x -> registry.register(x));
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
    public static class TestCommandHandlerWithWrongSynchronousHandler {

        @Handles(COMMAND_NAME)
        public Envelope handle1(Envelope envelope, Object invalidSecondArgument) {
            return null;
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithSynchronousHandler {

        @Handles(COMMAND_NAME)
        public Envelope handle1(Envelope envelope) {
            return envelope;
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithSynchronousHandlerDuplicate {

        @Handles(COMMAND_NAME)
        public Envelope handle1(Envelope envelope) {
            return envelope;
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithAsynchronousHandler {

        @Handles(COMMAND_NAME)
        public void handle1(Envelope envelope) {

        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithWrongParameter {

        @Handles(COMMAND_NAME)
        public void handle1(Object invalidSecondArgument) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandSynchronousHandlerWithWrongParameter {

        @Handles(COMMAND_NAME)
        public void handle1(Object invalidSecondArgument) {
        }

    }
}
