package uk.gov.justice.services.core.sender;

import static co.unruly.matchers.OptionalMatchers.contains;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationException;
import uk.gov.justice.services.core.envelope.RethrowingValidationExceptionHandler;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.json.JsonValue;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SenderProducerTest {


    @Mock
    private InjectionPoint injectionPoint;

    @Mock
    private Dispatcher dispatcher;

    @Mock
    private DispatcherCache dispatcherCache;

    @Mock
    private SystemUserUtil systemUserUtil;

    @Mock
    private JsonSchemaValidator jsonSchemaValidator;

    @Mock
    EnvelopePayloadTypeConverter envelopePayloadTypeConverter;

    @Mock
    JsonEnvelopeRepacker jsonEnvelopeRepacker;


    @InjectMocks
    private SenderProducer senderProducer;

    @Before
    public void setUp() throws Exception {
        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        senderProducer.objectMapper = new ObjectMapperProducer().objectMapper();
        senderProducer.envelopeValidationExceptionHandler = new RethrowingValidationExceptionHandler();
    }

    @Test
    public void shouldReturnSenderDelegatingToDispatcher() throws Exception {

        final Sender sender = senderProducer.produceSender(injectionPoint);
        final UUID id = randomUUID();
        final String name = "some-action";
        final String userId = "usr123";

        final JsonEnvelope envelopeToBeDispatched = envelopeFrom(
                metadataBuilder().withId(id).withName(name).withUserId(userId),
                createObjectBuilder()
                        .add("someField1", "value1")
                        .add("someField2", "value2"));
        final JsonEnvelope expectedResponse = envelopeFrom(metadataBuilder().withId(randomUUID()).withName("name"), createObjectBuilder());

        when(dispatcher.dispatch(envelopeToBeDispatched)).thenReturn(expectedResponse);
        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(envelopeToBeDispatched);
        when(jsonEnvelopeRepacker.repack(envelopeToBeDispatched)).thenReturn(envelopeToBeDispatched);


        sender.send(envelopeToBeDispatched);

        ArgumentCaptor<JsonEnvelope> dispatchedEnvelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(dispatcher).dispatch(dispatchedEnvelopeCaptor.capture());

        final JsonEnvelope dispatchedEnvelope = dispatchedEnvelopeCaptor.getValue();
        assertThat(envelopeToBeDispatched, sameInstance(dispatchedEnvelope));
        assertThat(dispatchedEnvelope.metadata().id(), is(id));
        assertThat(dispatchedEnvelope.metadata().name(), is(name));
        assertThat(dispatchedEnvelope.metadata().userId(), contains(userId));
    }

    @Test
    public void shouldDelegateAdminRequestSubstitutingUserId() throws Exception {

        final Sender sender = senderProducer.produceSender(injectionPoint);

        final JsonEnvelope originalEnvelope = envelopeFrom(
                metadataBuilder()
                        .withId(randomUUID())
                        .withName("some-action"),
                createObjectBuilder()
                        .add("someField1", "value1")
                        .add("someField2", "value2"));
        final JsonEnvelope envelopeWithSysUserId = envelopeFrom(metadataBuilder().withId(randomUUID()).withName("name"), createObjectBuilder());
        final JsonEnvelope expectedResponse = envelopeFrom(metadataBuilder().withId(randomUUID()).withName("name"), createObjectBuilder());

        when(systemUserUtil.asEnvelopeWithSystemUserId(originalEnvelope)).thenReturn(envelopeWithSysUserId);
        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(originalEnvelope);
        when(jsonEnvelopeRepacker.repack(originalEnvelope)).thenReturn(originalEnvelope);

        when(dispatcher.dispatch(envelopeWithSysUserId)).thenReturn(expectedResponse);

        sender.sendAsAdmin(originalEnvelope);

        verify(dispatcher).dispatch(envelopeWithSysUserId);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfResponseNotValidAgainstSchema() throws Exception {
        doThrow(new ValidationException(mock(Schema.class), "Message not valid against schema", "keyword", "location"))
                .when(jsonSchemaValidator).validate(any(String.class), any(String.class));


        senderProducer.envelopeValidationExceptionHandler = new RethrowingValidationExceptionHandler();

        exception.expect(EnvelopeValidationException.class);
        exception.expectMessage("Message not valid against schema");

        when(dispatcher.dispatch(any(JsonEnvelope.class)))
                .thenReturn(null);

        final JsonEnvelope envelope = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("some-action"),
                createObjectBuilder());

        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(envelope);
        when(jsonEnvelopeRepacker.repack(envelope)).thenReturn(envelope);

        senderProducer.produceSender(injectionPoint).send(envelope);
    }


    @Test
    public void shouldNotThrowExceptionIfPayloadAdheresToJsonSchema() {
        senderProducer.envelopeValidationExceptionHandler = new RethrowingValidationExceptionHandler();

        when(dispatcher.dispatch(any(JsonEnvelope.class)))
                .thenReturn(null);

        final JsonEnvelope envelope = envelopeFrom(
                metadataBuilder()
                        .withId(randomUUID())
                        .withName("some-action"),
                createObjectBuilder()
                        .add("someField1", "value1")
                        .add("someField2", "value2"));

        when(envelopePayloadTypeConverter.convert(any(Envelope.class), eq(JsonValue.class)))
                .thenReturn(envelope);
        when(jsonEnvelopeRepacker.repack(envelope)).thenReturn(envelope);

        senderProducer.produceSender(injectionPoint)
                .send(envelope);
    }

}
