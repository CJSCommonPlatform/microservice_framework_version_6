package uk.gov.justice.services.core.dispatcher;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
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
    private RequestResponseEnvelopeValidator requestResponseEnvelopeValidator;

    private DispatcherDelegate dispatcherDelegate;

    @Before
    public void setUp() throws Exception {
        dispatcherDelegate = new DispatcherDelegate(dispatcher, systemUserUtil, requestResponseEnvelopeValidator);
    }

    @Test
    public void requestMethodShouldDelegateToDispatcher() throws Exception {
        final JsonEnvelope envelope = envelope().build();

        dispatcherDelegate.request(envelope);

        verify(dispatcher).dispatch(envelope);
    }

    @Test
    public void requestMethodShouldValidateEnvelope() throws Exception {
        final JsonEnvelope response = envelope().build();
        when(dispatcher.dispatch(any(JsonEnvelope.class))).thenReturn(response);

        dispatcherDelegate.request(envelope().build());

        verify(requestResponseEnvelopeValidator).validateResponse(response);
    }

    @Test
    public void sendMethodShouldDelegateToDispatcher() throws Exception {

        final JsonEnvelope envelope = envelope().build();

        dispatcherDelegate.send(envelope);

        verify(dispatcher).dispatch(envelope);
    }

    @Test
    public void sendMethodShouldValidateEnvelope() throws Exception {

        final JsonEnvelope envelope = envelope().build();

        dispatcherDelegate.send(envelope);

        verify(requestResponseEnvelopeValidator).validateRequest(envelope);
    }

    @Test
    public void requestAsAdminMethodShouldDelegateEnvelopeReturnedBySystemUserUtil() {
        final JsonEnvelope envelope = envelope().build();
        final JsonEnvelope envelopeWithSysUserId = envelope().build();
        when(systemUserUtil.asEnvelopeWithSystemUserId(envelope)).thenReturn(envelopeWithSysUserId);

        dispatcherDelegate.requestAsAdmin(envelope);

        verify(dispatcher).dispatch(envelopeWithSysUserId);
    }

    @Test
    public void requestAsAdminMethodShouldValidateResponse() {
        final JsonEnvelope response = envelope().build();
        when(dispatcher.dispatch(any(JsonEnvelope.class))).thenReturn(response);

        dispatcherDelegate.requestAsAdmin(envelope().build());

        verify(requestResponseEnvelopeValidator).validateResponse(response);
    }

    @Test
    public void sendAsAdminMethodShouldDelegateEnvelopeReturnedBySystemUserUtil() {
        final JsonEnvelope envelope = envelope().build();
        final JsonEnvelope envelopeWithSysUserId = envelope().build();
        when(systemUserUtil.asEnvelopeWithSystemUserId(envelope)).thenReturn(envelopeWithSysUserId);

        dispatcherDelegate.sendAsAdmin(envelope);

        verify(dispatcher).dispatch(envelopeWithSysUserId);
    }

    @Test
    public void sendAsAdminMethodShouldValidateEnvelope() {
        final JsonEnvelope envelope = envelope().build();

        dispatcherDelegate.sendAsAdmin(envelope);

        verify(requestResponseEnvelopeValidator).validateRequest(envelope);
    }
}
