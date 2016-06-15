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
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.annotation.exception.MissingAnnotationException;
import uk.gov.justice.services.core.dispatcher.DispatcherProducer;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.jms.JmsSender;
import uk.gov.justice.services.core.jms.JmsSenderFactory;
import uk.gov.justice.services.core.util.TestInjectionPoint;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SenderProducerTest {

    @Mock
    private JmsSenderFactory jmsSenderFactory;

    @Mock
    private JmsSender primaryJmsSender;

    @Mock
    private JmsSender legacyJmsSender;

    @Mock
    private DispatcherProducer dispatcherProducer;

    private SenderProducer senderProducer;

    @Before
    public void setup() {
        senderProducer = new SenderProducer();
        senderProducer.jmsSenderFactory = jmsSenderFactory;
        senderProducer.dispatcherProducer = dispatcherProducer;
        senderProducer.componentDestination = new ComponentDestination();
    }

    @Test
    public void shouldReturnSenderWrapper() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestCommandApi.class);

        when(dispatcherProducer.produceSender(injectionPoint)).thenReturn(primaryJmsSender);
        when(jmsSenderFactory.createJmsSender(COMMAND_CONTROLLER)).thenReturn(legacyJmsSender);

        final Sender returnedSender = senderProducer.produce(injectionPoint);

        assertThat(returnedSender, notNullValue());

        final JsonEnvelope envelope = DefaultJsonEnvelope.envelopeFrom(null, null);

        returnedSender.send(envelope);

        verify(primaryJmsSender).send(envelope);
        verifyZeroInteractions(legacyJmsSender);


    }

    @Test
    public void shouldReturnSenderWrapper2() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestEventProcessor.class);

        when(dispatcherProducer.produceSender(injectionPoint)).thenReturn(primaryJmsSender);


        final Sender returnedSender = senderProducer.produce(injectionPoint);

        assertThat(returnedSender, notNullValue());

        final JsonEnvelope envelope = DefaultJsonEnvelope.envelopeFrom(null, null);

        returnedSender.send(envelope);

        verify(primaryJmsSender).send(envelope);

    }

    @Test
    public void senderWrapperShouldRedirectToLegacySenderWhenPrimaryThrowsException() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestCommandApi.class);

        when(dispatcherProducer.produceSender(injectionPoint)).thenReturn(primaryJmsSender);
        when(jmsSenderFactory.createJmsSender(COMMAND_CONTROLLER)).thenReturn(legacyJmsSender);
        final JsonEnvelope envelope = DefaultJsonEnvelope.envelopeFrom(null, null);
        doThrow(new MissingHandlerException("")).when(primaryJmsSender).send(envelope);

        final Sender returnedSender = senderProducer.produce(injectionPoint);

        returnedSender.send(envelope);

        verify(legacyJmsSender).send(envelope);

    }


    @Test(expected = MissingAnnotationException.class)
    public void shouldThrowExceptionWithInvalidHandler() throws Exception {
        senderProducer.produce(new TestInjectionPoint(TestInvalidHandler.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithInvalidComponent() throws Exception {

        senderProducer.produce(new TestInjectionPoint(TestCommandHandler.class));
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

    public static class TestInvalidHandler {
        public void dummyMethod() {

        }
    }

}