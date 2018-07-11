package uk.gov.justice.services.core.handler;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.handler.Handlers.handlerMethodsFrom;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Direct;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.UUID;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HandlerMethodTest {

    @Mock
    private AsynchronousCommandHandler asynchronousCommandHandler;

    @Mock
    private SynchronousCommandHandler synchronousCommandHandler;

    @Mock
    private CheckedExceptionThrowingCommandHandler checkedExcCommandHandler;

    @Spy
    private AsynchronousPojoCommandHandler asynchronousPojoCommandHandler = new AsynchronousPojoCommandHandler();

    @Spy
    private SynchronousPojoCommandHandler synchronousPojoCommandHandler = new SynchronousPojoCommandHandler();


    private JsonEnvelope envelope;

    @Before
    public void setup() throws Exception {
        envelope = testEnvelope("envelope.json");
    }

    @Test
    public void shouldExecuteAsynchronousHandlerMethod() throws Exception {
        Object result = asyncHandlerInstance().execute(envelope);
        verify(asynchronousCommandHandler).handles(envelope);
        assertThat(result, nullValue());
    }

    @Test
    public void shouldExecuteSynchronousHandlerMethod() throws Exception {
        when(synchronousCommandHandler.handles(envelope)).thenReturn(envelope);
        Object result = syncHandlerInstance().execute(envelope);
        assertThat(result, sameInstance(envelope));
    }

    @Test
    public void shouldHandlePojoAsynchronously() {
        final TestPojo testPojo = new TestPojo();
        final Metadata metadata = Envelope.metadataBuilder()
                .withId(UUID.randomUUID())
                .withName("test").build();
        final Envelope<TestPojo> testPojoEnvelope = Envelope.envelopeFrom(metadata, testPojo);
        Object result = asyncPojoHandlerInstance().execute(testPojoEnvelope);
        assertThat(result, nullValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldHandlePojoSynchronously() {
        final TestPojo testPojo = new TestPojo();

        final String payloadId = "3f47ab7e-aecc-4cec-9246-c32066ef5ba1";
        final String payloadName = "payload name";
        final long payloadVersion = 200L;

        testPojo.setPayloadId(payloadId);
        testPojo.setPayloadName(payloadName);
        testPojo.setPayloadVersion(payloadVersion);

        final Metadata metadata = Envelope.metadataBuilder()
                .withId(UUID.randomUUID())
                .withName("test").build();

        final Envelope<TestPojo> requestPojoEnvelope = Envelope.envelopeFrom(metadata, testPojo);

        final Envelope<TestPojo> result = syncPojoHandlerInstance().execute(requestPojoEnvelope);
        verify(synchronousPojoCommandHandler).handles(any(Envelope.class));
        final TestPojo resultPojo = result.payload();
        assertThat(resultPojo.getPayloadId(), is(payloadId));
        assertThat(resultPojo.getPayloadName(), is(payloadName));
        assertThat(resultPojo.getPayloadVersion(), is(payloadVersion));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldRethrowRuntimeException() throws Exception {
        doThrow(new RuntimeException("messageABC")).when(asynchronousCommandHandler).handles(envelope);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("messageABC");

        asyncHandlerInstance().execute(envelope);
    }

    @Test
    public void shouldWrapCheckedException() throws Exception {
        final Exception thrownException = new Exception("messageABC");
        doThrow(thrownException).when(checkedExcCommandHandler).handles(envelope);

        expectedException.expect(HandlerExecutionException.class);
        expectedException.expectCause(is(thrownException));

        new HandlerMethod(checkedExcCommandHandler, method(new CheckedExceptionThrowingCommandHandler(), "handles"), JsonEnvelope.class).execute(envelope);
    }

    @Test
    public void shouldReturnStringDescriptionOfHandlerInstanceAndMethod() {
        assertThat(asyncHandlerInstance().toString(), notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNullHandlerInstance() {
        new HandlerMethod(null, method(new AsynchronousCommandHandler(), "handles"), Void.TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNullMethod() {
        new HandlerMethod(asynchronousCommandHandler, null, Void.TYPE);
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithSynchronousMethod() {
        new HandlerMethod(asynchronousCommandHandler, method(new AsynchronousCommandHandler(), "handlesSync"), Void.TYPE);
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithAsynchronousMethod() {
        new HandlerMethod(synchronousCommandHandler, method(new SynchronousCommandHandler(), "handlesAsync"), JsonEnvelope.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldOnlyAcceptVoidOrEnvelopeReturnTypes() {
        new HandlerMethod(synchronousCommandHandler, method(new SynchronousCommandHandler(), "handles"), Object.class);
    }

    @Test
    public void shouldReturnFalseIfNoDirectAnnotation() throws Exception {
        final SynchronousCommandHandler handler = new SynchronousCommandHandler();
        assertThat(new HandlerMethod(new SynchronousCommandHandler(), method(handler, "handles"), JsonEnvelope.class).isDirect(), is(false));
    }

    @Test
    public void shouldReturnTrueIfDirectAnnotationPresent() throws Exception {
        final TestDirectComponentAHandler handler = new TestDirectComponentAHandler();
        assertThat(new HandlerMethod(handler, method(handler, "handles"), JsonEnvelope.class).isDirect(), is(true));
    }

    private HandlerMethod asyncHandlerInstance() {
        return new HandlerMethod(asynchronousCommandHandler, method(new AsynchronousCommandHandler(), "handles"), Void.TYPE);
    }

    private HandlerMethod asyncPojoHandlerInstance() {
        return new HandlerMethod(asynchronousPojoCommandHandler, method(new AsynchronousPojoCommandHandler(), "handles"), Void.TYPE);
    }

    private HandlerMethod syncPojoHandlerInstance() {
        return new HandlerMethod(synchronousPojoCommandHandler, method(new SynchronousPojoCommandHandler(), "handles"), Envelope.class);
    }

    private HandlerMethod syncHandlerInstance() {
        return new HandlerMethod(synchronousCommandHandler, method(new SynchronousCommandHandler(), "handles"), JsonEnvelope.class);
    }

    private Method method(final Object object, final String methofName) {
        return handlerMethodsFrom(object).stream()
                .filter(m -> methofName.equals(m.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Cannot find method with name %s", methofName)));
    }

    private JsonEnvelope testEnvelope(String fileName) throws IOException {
        String jsonString = Resources.toString(Resources.getResource("json/" + fileName), Charset.defaultCharset());
        return new DefaultJsonObjectEnvelopeConverter().asEnvelope(new StringToJsonObjectConverter().convert(jsonString));
    }

    public static class AsynchronousCommandHandler {

        @Handles("test-context.command.create-something")
        public void handles(final JsonEnvelope envelope) {
        }

        @Handles("test-context.command.create-something-else")
        public JsonEnvelope handlesSync(final JsonEnvelope envelope) {
            return envelope;
        }
    }

    public static class AsynchronousPojoCommandHandler {

        @Handles("test-context.command.create-something")
        public void handles(final Envelope<TestPojo> pojo) {
            assertThat(pojo.payload(), isA(TestPojo.class));
        }
    }

    public static class SynchronousPojoCommandHandler {

        @Handles("test-context.command.create-something")
        public Envelope<TestPojo> handles(final Envelope<TestPojo> pojo) {
            assertThat(pojo.payload(), isA(TestPojo.class));
            return pojo;
        }
    }

    public static class SynchronousCommandHandler {

        @Handles("test-context.command.create-something")
        public JsonEnvelope handles(final JsonEnvelope envelope) {
            return envelope;
        }

        @Handles("test-context.command.create-something-else")
        public void handlesAsync(final JsonEnvelope envelope) {
        }
    }

    public static class CheckedExceptionThrowingCommandHandler {

        @Handles("test-context.command.create-something")
        public JsonEnvelope handles(final JsonEnvelope envelope) throws Exception {
            return envelope;
        }
    }

    public static class PrivateMethodCommandHandler {

        @Handles("test-context.command.create-something")
        private JsonEnvelope handles(final JsonEnvelope envelope) {
            return envelope;
        }
    }

    @Direct(target = "not_used")
    @FrameworkComponent("COMPONENT_A")
    public static class TestDirectComponentAHandler {
        @Handles("something")
        public JsonEnvelope handles(final JsonEnvelope envelope) {
            return envelope;
        }
    }
}
