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

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.extension.RemoteServiceComponentFoundEvent;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.messaging.Envelope;
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
    private Envelope envelopeA;

    @Mock
    private Envelope envelopeB;

    @Mock
    private Envelope envelopeC;

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

        when(envelopeA.metadata()).thenReturn(metadataA);
        when(metadataA.name()).thenReturn(NAME_A);
        when(envelopeB.metadata()).thenReturn(metadataB);
        when(metadataB.name()).thenReturn(NAME_B);
        when(envelopeC.metadata()).thenReturn(metadataC);
        when(metadataC.name()).thenReturn(NAME_C);
    }

    @Test
    public void shouldReturnDispatcher() throws Exception {
        AsynchronousDispatcher dispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
        assertThat(dispatcher, notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNoAdaptor() throws Exception {
        doReturn(Object.class).when(commandControllerMember1).getDeclaringClass();
        dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
    }

    @Test
    public void shouldRegisterHandler() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, beanA));

        AsynchronousDispatcher dispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandApiInjectionPoint);
        assertThat(dispatcher, notNullValue());
        dispatcher.dispatch(envelopeA);
        assertThat(commandApiHandler.envelope, equalTo(envelopeA));
    }

    @Test
    public void shouldRegisterRemoteHandler() throws Exception {
        dispatcherProducer.register(new RemoteServiceComponentFoundEvent(QUERY_API, beanD));

        Requester requester = dispatcherProducer.produceRequester(queryApiInjectionPoint);
        assertThat(requester, notNullValue());
        Envelope result = requester.request(envelopeA);
        assertThat(result, equalTo(envelopeA));
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionForUnhandledCommand() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, beanA));
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_CONTROLLER, beanB));

        AsynchronousDispatcher controllerDispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
        controllerDispatcher.dispatch(envelopeC);
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionForAsyncMismatch() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, beanA));
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_CONTROLLER, beanB));

        SynchronousDispatcher syncDispatcher = dispatcherProducer.produceSynchronousDispatcher(commandControllerInjectionPoint1);
        syncDispatcher.dispatch(envelopeB);
        assertThat(commandControllerHandler.envelopeB, equalTo(envelopeB));

        AsynchronousDispatcher asyncDispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
        asyncDispatcher.dispatch(envelopeB);
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionForSyncMismatch() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, beanA));
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_CONTROLLER, beanB));

        AsynchronousDispatcher asyncDispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
        asyncDispatcher.dispatch(envelopeA);
        assertThat(commandControllerHandler.envelopeA, equalTo(envelopeA));

        SynchronousDispatcher syncDispatcher = dispatcherProducer.produceSynchronousDispatcher(commandControllerInjectionPoint1);
        syncDispatcher.dispatch(envelopeA);
    }

    @Test
    public void shouldRegisterDifferentHandlersForEachComponent() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, beanA));
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_CONTROLLER, beanB));

        AsynchronousDispatcher apiDispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandApiInjectionPoint);
        apiDispatcher.dispatch(envelopeA);
        assertThat(commandApiHandler.envelope, equalTo(envelopeA));
        assertThat(commandControllerHandler.envelopeA, nullValue());

        commandApiHandler.envelope = null;
        AsynchronousDispatcher controllerDispatcher = dispatcherProducer.produceAsynchronousDispatcher(commandControllerInjectionPoint1);
        controllerDispatcher.dispatch(envelopeA);
        assertThat(commandControllerHandler.envelopeA, equalTo(envelopeA));
        assertThat(commandApiHandler.envelope, nullValue());
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

        public Envelope envelope;

        @Handles(NAME_A)
        public void doSomething(Envelope envelope) {
            this.envelope = envelope;
        }

        @Handles(NAME_C)
        public void doSomethingElse(Envelope envelope) {
            this.envelope = null;
        }
    }

    @ServiceComponent(COMMAND_CONTROLLER)
    public static class TestCommandControllerHandler {

        public Envelope envelopeA;
        public Envelope envelopeB;

        @Handles(NAME_A)
        public void doSomethingA(Envelope envelope) {
            this.envelopeA = envelope;
        }

        @Handles(NAME_B)
        public Envelope doSomethingB(Envelope envelope) {
            this.envelopeB = envelope;
            return envelope;
        }
    }

    @Remote
    @ServiceComponent(QUERY_API)
    public static class TestRemoteQueryApiHandler {

        @Handles(NAME_A)
        public Envelope doSomething(Envelope envelope) {
            return envelope;
        }
    }
}
