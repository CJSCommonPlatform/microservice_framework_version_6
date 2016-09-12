package uk.gov.justice.services.core.sender;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
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
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.jms.JmsSender;
import uk.gov.justice.services.core.jms.SenderFactory;
import uk.gov.justice.services.core.util.TestInjectionPoint;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
    private SystemUserUtil systemUserUtil;

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
        senderProducer.systemUserUtil = systemUserUtil;
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
    public void shouldReturnSenderWrapperForLibraryComponent() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(LibraryComponent.class);

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

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Test
    public void shouldRethrowExceptionWhenPrimaryThrowsExceptionInEventProcessor() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestEventProcessor.class);
        final JsonEnvelope envelope = envelope().build();

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);

        doThrow(new MissingHandlerException("uhh can't handle that")).when(dispatcher).dispatch(envelope);

        exception.expect(MissingHandlerException.class);
        exception.expectMessage("uhh can't handle that");

        final Sender returnedSender = senderProducer.produce(injectionPoint);

        returnedSender.send(envelope);
    }
    @Test
    public void senderShouldSubstituteUserIdWhenSendingAsAdmin() throws Exception {

        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestEventProcessor.class);

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);

        final JsonEnvelope originalEnvelope = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope envelopeWithSysUserId = envelope().with(metadataWithDefaults()).build();
        when(systemUserUtil.asEnvelopeWithSystemUserId(originalEnvelope)).thenReturn(envelopeWithSysUserId);

        final Sender returnedSender = senderProducer.produce(injectionPoint);


        returnedSender.sendAsAdmin(originalEnvelope);

        verify(dispatcher).dispatch(envelopeWithSysUserId);

    }

    @Test
    public void legacySenderShouldSubstituteUserIdWhenSendingAsAdmin() throws Exception {
        final TestInjectionPoint injectionPoint = new TestInjectionPoint(TestCommandApi.class);

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        when(senderFactory.createSender(COMMAND_CONTROLLER)).thenReturn(legacyJmsSender);
        final JsonEnvelope envelope = envelope().with(metadataWithDefaults()).build();
        doThrow(new MissingHandlerException("")).when(dispatcher).dispatch(any(JsonEnvelope.class));

        final Sender returnedSender = senderProducer.produce(injectionPoint);

        returnedSender.sendAsAdmin(envelope);

        verify(legacyJmsSender).sendAsAdmin(envelope);
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

    @FrameworkComponent("NonComponentValue")
    public static class LibraryComponent {
        public void dummyMethod() {

        }
    }

    public static class TestInvalidHandler {
        public void dummyMethod() {

        }
    }

}
