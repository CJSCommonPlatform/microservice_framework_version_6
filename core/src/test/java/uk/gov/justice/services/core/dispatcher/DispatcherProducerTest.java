package uk.gov.justice.services.core.dispatcher;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.REMOTE;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.lang.reflect.Member;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherProducerTest {

    private static final String NAME_A = "test.commands.do-somethingA";
    private static final String NAME_B = "test.commands.do-somethingB";
    private static final String NAME_C = "test.commands.do-somethingC";

    private TestCommandApiHandler commandApiHandler;

    private TestCommandControllerHandler commandControllerHandler;

    private TestRemoteQueryApiHandler remoteQueryApiHandler;

    private DispatcherProducer dispatcherProducer;

    @Mock
    private InjectionPoint commandApiInjectionPoint;

    @Mock
    private InjectionPoint commandControllerInjectionPoint1;

    @Mock
    private InjectionPoint commandControllerInjectionPoint2;

    @Mock
    private InjectionPoint queryApiInjectionPoint;

    @Mock
    private Member commandApiMember;

    @Mock
    private Member commandControllerMember1;

    @Mock
    private Member commandControllerMember2;

    @Mock
    private Member queryApiMember;

    @Mock
    private ServiceComponentFoundEvent serviceComponentFoundEvent;

    @Mock
    private Bean<Object> beanA;

    @Mock
    private Bean<Object> beanB;

    @Mock
    private Bean<Object> beanD;

    @Mock
    private BeanManager beanManager;

    @Mock
    private Context context;

    @Mock
    private JsonEnvelope jsonEnvelopeA;

    @Mock
    private JsonEnvelope jsonEnvelopeB;

    @Mock
    private JsonEnvelope jsonEnvelopeC;

    @Mock
    private Metadata metadataA;

    @Mock
    private Metadata metadataB;

    @Mock
    private Metadata metadataC;

    @Before
    public void setup() {
        dispatcherProducer = new DispatcherProducer();
        commandApiHandler = new TestCommandApiHandler();
        commandControllerHandler = new TestCommandControllerHandler();
        remoteQueryApiHandler = new TestRemoteQueryApiHandler();

        dispatcherProducer.beanManager = beanManager;

        when(commandApiInjectionPoint.getMember()).thenReturn(commandApiMember);
        when(commandControllerInjectionPoint1.getMember()).thenReturn(commandControllerMember1);
        when(commandControllerInjectionPoint2.getMember()).thenReturn(commandControllerMember2);
        when(queryApiInjectionPoint.getMember()).thenReturn(queryApiMember);

        doReturn(TestCommandApiAdaptor.class).when(commandApiMember).getDeclaringClass();
        doReturn(TestCommandControllerAdaptor1.class).when(commandControllerMember1).getDeclaringClass();
        doReturn(TestCommandControllerAdaptor2.class).when(commandControllerMember2).getDeclaringClass();
        doReturn(TestRemoteQueryApiHandler.class).when(queryApiMember).getDeclaringClass();

        when(beanManager.getContext(any())).thenReturn(context);
        when(context.get(eq(beanA), any())).thenReturn(commandApiHandler);
        when(context.get(eq(beanB), any())).thenReturn(commandControllerHandler);
        when(context.get(eq(beanD), any())).thenReturn(remoteQueryApiHandler);

        when(jsonEnvelopeA.metadata()).thenReturn(metadataA);
        when(metadataA.name()).thenReturn(NAME_A);
        when(jsonEnvelopeB.metadata()).thenReturn(metadataB);
        when(metadataB.name()).thenReturn(NAME_B);
        when(jsonEnvelopeC.metadata()).thenReturn(metadataC);
        when(metadataC.name()).thenReturn(NAME_C);
    }

    @Test
    public void shouldReturnDispatcher() throws Exception {
        AsynchronousDispatcher dispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
        assertThat(dispatcher, notNullValue());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWithNoAdaptor() throws Exception {
        doReturn(Object.class).when(commandControllerMember1).getDeclaringClass();
        dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
    }

    @Test
    public void shouldRegisterHandler() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, beanA, LOCAL));

        AsynchronousDispatcher dispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandApiInjectionPoint);
        assertThat(dispatcher, notNullValue());
        dispatcher.dispatch(jsonEnvelopeA);
        assertThat(commandApiHandler.jsonEnvelope, equalTo(jsonEnvelopeA));
    }

    @Test
    public void shouldRegisterRemoteHandler() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(QUERY_API, beanD, REMOTE));

        Requester requester = dispatcherProducer.produceRequester(queryApiInjectionPoint);
        assertThat(requester, notNullValue());
        JsonEnvelope result = requester.request(jsonEnvelopeA);
        assertThat(result, equalTo(jsonEnvelopeA));
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionForUnhandledCommand() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, beanA, LOCAL));
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_CONTROLLER, beanB, LOCAL));

        AsynchronousDispatcher controllerDispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
        controllerDispatcher.dispatch(jsonEnvelopeC);
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionForAsyncMismatch() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, beanA, LOCAL));
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_CONTROLLER, beanB, LOCAL));

        SynchronousDispatcher syncDispatcher = dispatcherProducer.produceSynchronousDispatcher(commandControllerInjectionPoint1);
        syncDispatcher.dispatch(jsonEnvelopeB);
        assertThat(commandControllerHandler.jsonEnvelopeB, equalTo(jsonEnvelopeB));

        AsynchronousDispatcher asyncDispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
        asyncDispatcher.dispatch(jsonEnvelopeB);
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionForSyncMismatch() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, beanA, LOCAL));
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_CONTROLLER, beanB, LOCAL));

        AsynchronousDispatcher asyncDispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
        asyncDispatcher.dispatch(jsonEnvelopeA);
        assertThat(commandControllerHandler.jsonEnvelopeA, equalTo(jsonEnvelopeA));

        SynchronousDispatcher syncDispatcher = dispatcherProducer.produceSynchronousDispatcher(commandControllerInjectionPoint1);
        syncDispatcher.dispatch(jsonEnvelopeA);
    }

    @Test
    public void shouldRegisterDifferentHandlersForEachComponent() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, beanA, LOCAL));
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_CONTROLLER, beanB, LOCAL));

        AsynchronousDispatcher apiDispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandApiInjectionPoint);
        apiDispatcher.dispatch(jsonEnvelopeA);
        assertThat(commandApiHandler.jsonEnvelope, equalTo(jsonEnvelopeA));
        assertThat(commandControllerHandler.jsonEnvelopeA, nullValue());

        commandApiHandler.jsonEnvelope = null;
        AsynchronousDispatcher controllerDispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
        controllerDispatcher.dispatch(jsonEnvelopeA);
        assertThat(commandControllerHandler.jsonEnvelopeA, equalTo(jsonEnvelopeA));
        assertThat(commandApiHandler.jsonEnvelope, nullValue());
    }

    @Adapter(COMMAND_API)
    public static class TestCommandApiAdaptor {
    }

    @Adapter(COMMAND_CONTROLLER)
    public static class TestCommandControllerAdaptor1 {
    }

    @Adapter(COMMAND_CONTROLLER)
    public static class TestCommandControllerAdaptor2 {
    }

    @ServiceComponent(COMMAND_API)
    public static class TestCommandApiHandler {

        public JsonEnvelope jsonEnvelope;

        @Handles(NAME_A)
        public void doSomething(JsonEnvelope jsonEnvelope) {
            this.jsonEnvelope = jsonEnvelope;
        }

        @Handles(NAME_C)
        public void doSomethingElse(JsonEnvelope jsonEnvelope) {
            this.jsonEnvelope = null;
        }
    }

    @ServiceComponent(COMMAND_CONTROLLER)
    public static class TestCommandControllerHandler {

        public JsonEnvelope jsonEnvelopeA;
        public JsonEnvelope jsonEnvelopeB;

        @Handles(NAME_A)
        public void doSomethingA(JsonEnvelope jsonEnvelope) {
            this.jsonEnvelopeA = jsonEnvelope;
        }

        @Handles(NAME_B)
        public JsonEnvelope doSomethingB(JsonEnvelope jsonEnvelope) {
            this.jsonEnvelopeB = jsonEnvelope;
            return jsonEnvelope;
        }
    }

    @Remote
    @ServiceComponent(QUERY_API)
    public static class TestRemoteQueryApiHandler {

        @Handles(NAME_A)
        public JsonEnvelope doSomething(JsonEnvelope jsonEnvelope) {
            return jsonEnvelope;
        }
    }
}
