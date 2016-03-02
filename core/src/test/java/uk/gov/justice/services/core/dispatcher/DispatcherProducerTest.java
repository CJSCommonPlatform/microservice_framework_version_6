package uk.gov.justice.services.core.dispatcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.reflect.Member;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherProducerTest {

    private static final String NAME = "test.commands.do-something";

    @Mock
    private Metadata metadata;

    private TestCommandApiHandler handlerInstance;

    private DispatcherProducer dispatcherProducer;

    @Mock
    private InjectionPoint commandApiInjectionPoint;

    @Mock
    private InjectionPoint commandControllerInjectionPoint1;

    @Mock
    private InjectionPoint commandControllerInjectionPoint2;

    @Mock
    private Member commandApiMember;

    @Mock
    private Member commandControllerMember1;

    @Mock
    private Member commandControllerMember2;

    @Mock
    private ServiceComponentFoundEvent serviceComponentFoundEvent;

    @Mock
    private Bean bean;

    @Mock
    private AsynchronousDispatcher commandApiDispatcher;

    @Mock
    private HandlerRegistry commandApiRegistry;

    @Mock
    private HandlerRegistry commandControllerRegistry;

    @Mock
    private BeanManager beanManager;

    @Mock
    private Context context;

    @Mock
    private Envelope envelope;

    @Before
    public void setup() {
        dispatcherProducer = new DispatcherProducer();
        handlerInstance = new TestCommandApiHandler();

        dispatcherProducer.beanManager = beanManager;

        when(commandApiInjectionPoint.getMember()).thenReturn(commandApiMember);
        when(commandControllerInjectionPoint1.getMember()).thenReturn(commandControllerMember1);
        when(commandControllerInjectionPoint2.getMember()).thenReturn(commandControllerMember2);

        doReturn(TestCommandApiAdaptor.class).when(commandApiMember).getDeclaringClass();
        doReturn(TestCommandControllerAdaptor1.class).when(commandControllerMember1).getDeclaringClass();
        doReturn(TestCommandControllerAdaptor2.class).when(commandControllerMember2).getDeclaringClass();

        when(beanManager.getContext(any())).thenReturn(context);
        when(context.get(any(), any())).thenReturn(handlerInstance);

        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);

    }

    @Test
    public void shouldReturnDispatcher() throws Exception {
        Dispatcher dispatcher = dispatcherProducer.produce(commandControllerInjectionPoint1);
        assertThat(dispatcher, notNullValue());
    }

    @Test
    public void shouldReturnExistingDispatcher() throws Exception {
        Dispatcher dispatcher = dispatcherProducer.produce(commandControllerInjectionPoint1);
        assertThat(dispatcher, notNullValue());

        Dispatcher anotherDispatcher = dispatcherProducer.produce(commandControllerInjectionPoint2);
        assertThat(anotherDispatcher, sameInstance(dispatcher));
    }

    @Test
    public void shouldReturnADifferentDispatcher() throws Exception {
        Dispatcher dispatcher = dispatcherProducer.produce(commandApiInjectionPoint);
        assertThat(dispatcher, notNullValue());

        Dispatcher anotherDispatcher = dispatcherProducer.produce(commandControllerInjectionPoint2);
        assertThat(anotherDispatcher, not(equalTo(dispatcher)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNoAdaptor() throws Exception {
        doReturn(Object.class).when(commandControllerMember1).getDeclaringClass();
        dispatcherProducer.produce(commandControllerInjectionPoint1);
    }

    @Test
    public void shouldRegisterHandler() throws Exception {
        dispatcherProducer.register(new ServiceComponentFoundEvent(COMMAND_API, bean));

        Dispatcher dispatcher = dispatcherProducer.produce(commandApiInjectionPoint);
        assertThat(dispatcher, notNullValue());
        dispatcher.dispatch(envelope);
        assertThat(handlerInstance.envelope, equalTo(envelope));
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

        @Handles("test.commands.do-something")
        public void doSomething(Envelope envelope) {
            this.envelope = envelope;
        }

        @Handles("test.commands.do-something-else")
        public void doSomethingElse(Envelope envelope) {
            this.envelope = null;
        }

    }
}