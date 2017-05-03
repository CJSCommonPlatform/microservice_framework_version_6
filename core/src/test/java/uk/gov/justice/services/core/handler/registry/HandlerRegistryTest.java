package uk.gov.justice.services.core.handler.registry;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.core.annotation.Direct;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.HandlerMethod;
import uk.gov.justice.services.core.handler.registry.exception.DuplicateHandlerException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Unit tests for the HandlerRegistry class.
 */
@RunWith(MockitoJUnitRunner.class)
public class HandlerRegistryTest {

    private static final String COMMAND_NAME = "test.command.mock-command";

    @Mock
    private Logger logger;

    @Mock
    private TestCommandHandler commandHandler;

    @Mock
    private TestCommandHandlerDuplicate commandHandlerDuplicate;

    private HandlerRegistry registry;

    @Test
    public void shouldReturnMethodOfTheRegisteredAsynchronousHandler() {
        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        createRegistryWith(testCommandHandler);
        final HandlerMethod handlerMethod = registry.get(COMMAND_NAME);
        assertHandlerMethodInvokesHandler(handlerMethod, testCommandHandler);

        assertLogStatement(COMMAND_NAME);
    }

    @Test
    public void shouldReturnMethodOfTheRegisteredSynchronousHandler() {
        final TestCommandHandlerWithSynchronousHandler testCommandHandlerWithSynchronousHandler = new TestCommandHandlerWithSynchronousHandler();
        createRegistryWith(testCommandHandlerWithSynchronousHandler);
        final HandlerMethod handlerMethod = registry.get(COMMAND_NAME);
        assertHandlerMethodInvokesHandler(handlerMethod, testCommandHandlerWithSynchronousHandler);

        assertLogStatement(COMMAND_NAME);
    }


    @Test
    public void shouldReturnMethodOfTheAllEventsHandler() {
        final TestAllEventsHandler testAllEventsHandler = new TestAllEventsHandler();
        createRegistryWith(testAllEventsHandler);
        final HandlerMethod handlerMethod = registry.get("some.name");

        assertHandlerMethodInvokesHandler(handlerMethod, testAllEventsHandler);

        assertLogStatement("*");
    }

    @Test
    public void namedHandlerShouldTakePriorityOverAllHandler() {
        final TestAllEventsHandler testAllEventsHandler = new TestAllEventsHandler();
        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        createRegistryWith(testAllEventsHandler, testCommandHandler);
        final HandlerMethod handlerMethod = registry.get(COMMAND_NAME);

        assertHandlerMethodInvokesHandler(handlerMethod, testCommandHandler);
        assertThat(testAllEventsHandler.firstRecordedEnvelope(), nullValue());

        assertLogStatement("*");
    }


    @Test
    public void directHandlerShouldReplaceNonDirectHandler() {
        TestComponentAHandler testComponentAHandler = new TestComponentAHandler();
        TestDirectComponentAHandler testDirectComponentAHandler = new TestDirectComponentAHandler();

        createRegistryWith(testComponentAHandler, testDirectComponentAHandler);
        final HandlerMethod handlerMethod = registry.get(COMMAND_NAME);
        assertHandlerMethodInvokesHandler(handlerMethod, testDirectComponentAHandler);
    }

    @Test
    public void shouldIgnoreNonDirectHandlerIfDirectOneRegistered() throws Exception {

        TestComponentAHandler testComponentAHandler = new TestComponentAHandler();
        TestDirectComponentAHandler testDirectComponentAHandler = new TestDirectComponentAHandler();

        createRegistryWith(testDirectComponentAHandler, testComponentAHandler);
        final HandlerMethod handlerMethod = registry.get(COMMAND_NAME);
        assertHandlerMethodInvokesHandler(handlerMethod, testDirectComponentAHandler);
    }

    @Test(expected = DuplicateHandlerException.class)
    public void shouldThrowExceptionIfAttemptingToRegisterDuplicateDirectHandler() throws Exception {

        createRegistryWith(new TestDirectComponentAHandler(), new TestDirectComponentAHandlerDuplicate());
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithMultipleArgumentsAsynchronousHandler() {
        createRegistryWith(new TestCommandHandlerWithWrongHandler());
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithMultipleArgumentsSynchronousHandler() {
        createRegistryWith(new TestCommandHandlerWithWrongSynchronousHandler());
    }

    @Test(expected = DuplicateHandlerException.class)
    public void shouldThrowExceptionWithDuplicateAsynchronousHandlers() {
        createRegistryWith(new TestCommandHandler(), new TestCommandHandlerDuplicate());
    }

    @Test(expected = DuplicateHandlerException.class)
    public void shouldThrowExceptionWithDuplicateSynchronousHandlers() {
        createRegistryWith(new TestCommandHandlerWithSynchronousHandler(), new TestCommandHandlerWithSynchronousHandlerDuplicate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithAsynchronousWrongParameters() {
        createRegistryWith(new TestCommandHandler(), new TestCommandHandlerWithWrongParameter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithSynchronousWrongParameters() {
        createRegistryWith(new TestCommandHandlerWithSynchronousHandler(), new TestCommandSynchronousHandlerWithWrongParameter());
    }

    private void assertHandlerMethodInvokesHandler(final HandlerMethod handlerMethod, final TestEnvelopeRecorder handler) {
        assertThat(handlerMethod, notNullValue());

        final JsonEnvelope envelope = envelope().build();
        handlerMethod.execute(envelope);

        assertThat(handler.firstRecordedEnvelope(), sameInstance(envelope));
    }

    private void createRegistryWith(Object... handlers) {
        registry = new HandlerRegistry(logger);
        asList(handlers).forEach(x -> registry.register(x));
    }

    public static class MockCommand {

    }

    private void assertLogStatement(final String name) {
        verify(logger).info(eq("Registering handler {}, {}"), eq(name), anyString());
    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandler extends TestEnvelopeRecorder {

        @Handles(COMMAND_NAME)
        public void handle(JsonEnvelope envelope) {
            record(envelope);
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
    public static class TestCommandHandlerWithSynchronousHandler extends TestEnvelopeRecorder {

        @Handles(COMMAND_NAME)
        public JsonEnvelope handle1(JsonEnvelope envelope) {
            record(envelope);
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

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestAllEventsHandler extends TestEnvelopeRecorder {

        @Handles("*")
        public void handle(JsonEnvelope envelope) {
            record(envelope);
        }

    }

    @FrameworkComponent("COMPONENT_A")
    public static class TestComponentAHandler extends TestEnvelopeRecorder {
        @Handles(COMMAND_NAME)
        public JsonEnvelope handle1(JsonEnvelope envelope) {
            record(envelope);
            return envelope;
        }
    }

    @Direct(target = "not_used")
    @FrameworkComponent("COMPONENT_A")
    public static class TestDirectComponentAHandler extends TestEnvelopeRecorder {
        @Handles(COMMAND_NAME)
        public JsonEnvelope handle1(JsonEnvelope envelope) {
            record(envelope);
            return envelope;
        }
    }

    @Direct(target = "not_used")
    @FrameworkComponent("COMPONENT_A")
    public static class TestDirectComponentAHandlerDuplicate extends TestEnvelopeRecorder {
        @Handles(COMMAND_NAME)
        public JsonEnvelope handle1(JsonEnvelope envelope) {
            record(envelope);
            return envelope;
        }
    }


}
