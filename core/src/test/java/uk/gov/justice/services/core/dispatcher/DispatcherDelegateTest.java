package uk.gov.justice.services.core.dispatcher;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonValue;

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
    private JsonEnvelopeRepacker jsonEnvelopeRepacker;

    @Mock
    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;

    @Mock
    private RequestResponseEnvelopeValidator requestResponseEnvelopeValidator;

    private DispatcherDelegate dispatcherDelegate;

    @Before
    public void setUp() throws Exception {
        dispatcherDelegate = new DispatcherDelegate(dispatcher, systemUserUtil, requestResponseEnvelopeValidator, envelopePayloadTypeConverter, jsonEnvelopeRepacker);
    }

    @Test
    public void shouldDelegateToDispatcherWithRequestMethod() throws Exception {
        final JsonEnvelope envelope = mock(JsonEnvelope.class);

        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(envelope);
        when(jsonEnvelopeRepacker.repack(envelope)).thenReturn(envelope);


        dispatcherDelegate.request(envelope);

        verify(dispatcher).dispatch(envelope);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDispatchEnvelopeWithPojoPayload() {

        final Envelope<JsonValue> jsonValueEnvelope = mock(Envelope.class);

        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(jsonValueEnvelope);
        when(jsonEnvelopeRepacker.repack(jsonValueEnvelope)).thenReturn(mock(JsonEnvelope.class));

        final Envelope<Object> envelope = mock(Envelope.class);

        dispatcherDelegate.send(envelope);

        verify(dispatcher).dispatch(any(JsonEnvelope.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDispatchAdminEnvelopeWithPojoPayload() {

        final Envelope<JsonValue> jsonValueEnvelope = mock(Envelope.class);

        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(jsonValueEnvelope);
        when(jsonEnvelopeRepacker.repack(jsonValueEnvelope)).thenReturn(mock(JsonEnvelope.class));

        final Envelope<Object> envelope = mock(Envelope.class);

        dispatcherDelegate.sendAsAdmin(envelope);

        verify(dispatcher).dispatch(any(JsonEnvelope.class));
    }

    @Test
    public void shouldValidateJsonEnvelopeWithRequestMethod() throws Exception {
        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope response = mock(JsonEnvelope.class);

        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(envelope);
        when(jsonEnvelopeRepacker.repack(envelope)).thenReturn(envelope);

        when(dispatcher.dispatch(envelope)).thenReturn(response);

        dispatcherDelegate.request(envelope);

        verify(requestResponseEnvelopeValidator).validateResponse(response);
    }

    @Test
    public void shouldValidatePojoEnvelopeWithRequestMethod() throws Exception {

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope response = mock(JsonEnvelope.class);
        final Envelope envelope = mock(Envelope.class);

        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(jsonEnvelope);
        when(jsonEnvelopeRepacker.repack(jsonEnvelope)).thenReturn(jsonEnvelope);

        when(dispatcher.dispatch(jsonEnvelope)).thenReturn(response);

        dispatcherDelegate.request(envelope, Object.class);

        verify(requestResponseEnvelopeValidator).validateResponse(response);
    }

    @Test
    public void shouldDelegateToDispatcherWithSendMethod() throws Exception {

        final JsonEnvelope envelope = mock(JsonEnvelope.class);

        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(envelope);
        when(jsonEnvelopeRepacker.repack(envelope)).thenReturn(envelope);

        dispatcherDelegate.send(envelope);

        verify(dispatcher).dispatch(envelope);
    }

    @Test
    public void shouldValidateEnvelopeWithSendMethod() throws Exception {

        final JsonEnvelope envelope = mock(JsonEnvelope.class);

        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(envelope);
        when(jsonEnvelopeRepacker.repack(envelope)).thenReturn(envelope);

        dispatcherDelegate.send(envelope);

        verify(requestResponseEnvelopeValidator).validateRequest(envelope);
    }

    @Test
    public void shouldDelegateEnvelopeReturnedBySystemUserUtilWithRequestAsAdminMethod() {
        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope envelopeWithSysUserId = mock(JsonEnvelope.class);
        when(systemUserUtil.asEnvelopeWithSystemUserId(envelope)).thenReturn(envelopeWithSysUserId);

        dispatcherDelegate.requestAsAdmin(envelope);

        verify(dispatcher).dispatch(envelopeWithSysUserId);
    }

    @Test
    public void shouldValidateResponseWithRequestAsAdminMethod() {
        final JsonEnvelope response = mock(JsonEnvelope.class);
        when(dispatcher.dispatch(any(JsonEnvelope.class))).thenReturn(response);

        dispatcherDelegate.requestAsAdmin(mock(JsonEnvelope.class));

        verify(requestResponseEnvelopeValidator).validateResponse(response);
    }

    @Test
    public void shouldDelegateEnvelopeReturnedBySystemUserUtilWithSendAsAdminMethod() {
        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope envelopeWithSysUserId = mock(JsonEnvelope.class);
        when(systemUserUtil.asEnvelopeWithSystemUserId(envelope)).thenReturn(envelopeWithSysUserId);

        dispatcherDelegate.sendAsAdmin(envelope);

        verify(dispatcher).dispatch(envelopeWithSysUserId);
    }

    @Test
    public void shouldValidateEnvelopeWithSendAsAdminMethod() {
        final JsonEnvelope envelope = mock(JsonEnvelope.class);

        dispatcherDelegate.sendAsAdmin(envelope);

        verify(requestResponseEnvelopeValidator).validateRequest(envelope);
    }

    @Test
    public void shouldValidateResponseWithPojoRequestAsAdminMethod() {
        final Envelope<JsonValue> jsonValueEnvelope = mock(Envelope.class);
        final JsonEnvelope response = mock(JsonEnvelope.class);

        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(jsonValueEnvelope);
        when(jsonEnvelopeRepacker.repack(jsonValueEnvelope)).thenReturn(mock(JsonEnvelope.class));

        when(dispatcher.dispatch(any(JsonEnvelope.class))).thenReturn(response);

        final Envelope<Object> envelope = mock(Envelope.class);

        dispatcherDelegate.requestAsAdmin(envelope, Object.class);

        verify(requestResponseEnvelopeValidator).validateResponse(response);
    }
}
