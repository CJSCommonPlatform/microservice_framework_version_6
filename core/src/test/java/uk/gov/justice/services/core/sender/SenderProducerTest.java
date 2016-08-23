package uk.gov.justice.services.core.sender;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_API;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.jms.JmsSender;
import uk.gov.justice.services.core.jms.SenderFactory;
import uk.gov.justice.services.core.util.TestInjectionPoint;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SenderProducerTest {

    @Mock
    private SenderFactory senderFactory;

    @Mock
    private JmsSender legacyJmsSender;

    @Mock
    Dispatcher dispatcher;

    @Mock
    private DispatcherCache dispatcherCache;

    private SenderProducer senderProducer;

    @Before
    public void setup() {
        senderProducer = new SenderProducer();
        senderProducer.senderFactory = senderFactory;
        senderProducer.dispatcherCache = dispatcherCache;
        senderProducer.componentDestination = new ComponentDestination();
    }

    @Test
    public void shouldReturnSenderWrapper() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestCommandApi.class);

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        when(senderFactory.createSender(COMMAND_CONTROLLER)).thenReturn(legacyJmsSender);

        final Sender returnedSender = senderProducer.produce(injectionPoint);

        assertThat(returnedSender, notNullValue());

        final JsonEnvelope envelope = envelope().build();

        returnedSender.send(envelope);

        verify(dispatcher).dispatch(envelope);
        verifyZeroInteractions(legacyJmsSender);
    }

    @Test
    public void shouldReturnSenderWrapperForEventProcessor() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestEventProcessor.class);

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);

        final Sender returnedSender = senderProducer.produce(injectionPoint);

        assertThat(returnedSender, notNullValue());

        final JsonEnvelope envelope = envelope().build();

        returnedSender.send(envelope);

        verify(dispatcher).dispatch(envelope);
    }

    @Test
    public void shouldReturnSenderWrapperForEventApi() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestEventAPI.class);

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);

        final Sender returnedSender = senderProducer.produce(injectionPoint);

        assertThat(returnedSender, notNullValue());

        final JsonEnvelope envelope = envelope().build();

        returnedSender.send(envelope);

        verify(dispatcher).dispatch(envelope);
    }

    @Test
    public void senderWrapperShouldRedirectToLegacySenderWhenPrimaryThrowsException() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestCommandApi.class);

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        when(senderFactory.createSender(COMMAND_CONTROLLER)).thenReturn(legacyJmsSender);
        final JsonEnvelope envelope = envelope().build();
        doThrow(new MissingHandlerException("")).when(dispatcher).dispatch(envelope);

        final Sender returnedSender = senderProducer.produce(injectionPoint);

        returnedSender.send(envelope);

        verify(legacyJmsSender).send(envelope);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithInvalidComponent() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestCommandHandler.class);
        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        senderProducer.produce(injectionPoint);
    }

    @ServiceComponent(COMMAND_API)
    public static class TestCommandApi {
        public void dummyMethod() {

        }
    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandler {
        public void dummyMethod() {

        }

    }

    @ServiceComponent(EVENT_PROCESSOR)
    public static class TestEventProcessor {
        public void dummyMethod() {

        }
    }

    @ServiceComponent(EVENT_API)
    public static class TestEventAPI {
        public void dummyMethod() {

        }
    }

    public static class TestInvalidHandler {
        public void dummyMethod() {

        }
    }

}
