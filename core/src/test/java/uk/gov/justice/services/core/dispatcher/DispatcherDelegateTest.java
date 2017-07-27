package uk.gov.justice.services.core.dispatcher;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherDelegateTest {

    @Mock
    private Dispatcher dispatcher;

    @Mock
    private SystemUserUtil systemUserUtil;

    @Mock
    private EnvelopeValidator envelopeValidator;

    private DispatcherDelegate dispatcherDelegate;

    @Before
    public void setUp() throws Exception {
        dispatcherDelegate = new DispatcherDelegate(dispatcher, systemUserUtil, envelopeValidator);
    }

    @Test
    public void requestMethodShouldDelegateToDispatcher() throws Exception {
        final JsonEnvelope envelope = mock(JsonEnvelope.class);

        dispatcherDelegate.request(envelope);

        verify(dispatcher).dispatch(envelope);
    }


    @Test
    public void requestMethodShouldValidateEnvelope() throws Exception {
        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope response = mock(JsonEnvelope.class);
        when(dispatcher.dispatch(envelope)).thenReturn(response);

        dispatcherDelegate.request(envelope);

        verify(envelopeValidator).validate(response);
    }

    @Test
    public void sendMethodShouldDelegateToDispatcher() throws Exception {

        final JsonEnvelope envelope = mock(JsonEnvelope.class);

        dispatcherDelegate.send(envelope);

        verify(dispatcher).dispatch(envelope);
    }

    @Test
    public void sendMethodShouldValidateEnvelope() throws Exception {

        final JsonEnvelope envelope = mock(JsonEnvelope.class);

        dispatcherDelegate.send(envelope);

        verify(envelopeValidator).validate(envelope);
    }

    @Test
    public void requestAsAdminMethodShouldDelegateEnvelopeReturnedBySystemUserUtil() {
        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope envelopeWithSysUserId = mock(JsonEnvelope.class);
        when(systemUserUtil.asEnvelopeWithSystemUserId(envelope)).thenReturn(envelopeWithSysUserId);

        dispatcherDelegate.requestAsAdmin(envelope);

        verify(dispatcher).dispatch(envelopeWithSysUserId);
    }


    @Test
    public void requestAsAdminMethodShouldValidateResponse() {
        final JsonEnvelope response = mock(JsonEnvelope.class);
        when(dispatcher.dispatch(any(JsonEnvelope.class))).thenReturn(response);

        dispatcherDelegate.requestAsAdmin(mock(JsonEnvelope.class));

        verify(envelopeValidator).validate(response);
    }


    @Test
    public void sendAsAdminMethodShouldDelegateEnvelopeReturnedBySystemUserUtil() {
        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope envelopeWithSysUserId = mock(JsonEnvelope.class);
        when(systemUserUtil.asEnvelopeWithSystemUserId(envelope)).thenReturn(envelopeWithSysUserId);

        dispatcherDelegate.sendAsAdmin(envelope);

        verify(dispatcher).dispatch(envelopeWithSysUserId);
    }

    @Test
    public void sendAsAdminMethodShouldValidateEnvelope() {
        final JsonEnvelope envelope = mock(JsonEnvelope.class);

        dispatcherDelegate.sendAsAdmin(envelope);

        verify(envelopeValidator).validate(envelope);
    }


}