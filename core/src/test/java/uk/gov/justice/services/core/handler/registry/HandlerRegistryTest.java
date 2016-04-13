package uk.gov.justice.services.core.handler.registry;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.handler.HandlerMethod.ASYNCHRONOUS;
import static uk.gov.justice.services.core.handler.HandlerMethod.SYNCHRONOUS;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.handler.registry.exception.DuplicateHandlerException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.JsonEnvelope;

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
    public void shouldReturnTheRegisteredAsynchronousHandler() {
        createRegistry(new TestCommandHandler());
        assertThat(registry.get(COMMAND_NAME, ASYNCHRONOUS), notNullValue());
    }

    @Test
    public void shouldReturnTheRegisteredSynchronousHandler() {
        createRegistry(new TestCommandHandlerWithSynchronousHandler());
        assertThat(registry.get(COMMAND_NAME, SYNCHRONOUS), notNullValue());
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionForAsyncMismatch() {
        createRegistry(new TestCommandHandler());
        assertThat(registry.get(COMMAND_NAME, SYNCHRONOUS), nullValue());
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionForSyncMismatch() {
        createRegistry(new TestCommandHandlerWithSynchronousHandler());
        assertThat(registry.get(COMMAND_NAME, ASYNCHRONOUS), nullValue());
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
        public void handle(JsonEnvelope envelope) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerDuplicate {

        @Handles(COMMAND_NAME)
        public void handle(JsonEnvelope envelope) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithWrongHandler {

        @Handles(COMMAND_NAME)
        public void handle1(JsonEnvelope envelope, Object invalidSecondArgument) {
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithWrongSynchronousHandler {

        @Handles(COMMAND_NAME)
        public JsonEnvelope handle1(JsonEnvelope envelope, Object invalidSecondArgument) {
            return null;
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithSynchronousHandler {

        @Handles(COMMAND_NAME)
        public JsonEnvelope handle1(JsonEnvelope envelope) {
            return envelope;
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithSynchronousHandlerDuplicate {

        @Handles(COMMAND_NAME)
        public JsonEnvelope handle1(JsonEnvelope envelope) {
            return envelope;
        }

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandlerWithAsynchronousHandler {

        @Handles(COMMAND_NAME)
        public void handle1(JsonEnvelope envelope) {

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
