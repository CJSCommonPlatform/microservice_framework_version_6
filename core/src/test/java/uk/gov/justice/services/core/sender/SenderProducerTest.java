package uk.gov.justice.services.core.sender;

import static co.unruly.matchers.OptionalMatchers.contains;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWith;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWithMemberAsFirstMethodOf;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.annotation.ComponentNameUtilTest;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationException;
import uk.gov.justice.services.core.envelope.RethrowingValidationExceptionHandler;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.MemberInjectionPoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;

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

    private InjectionPoint injectionPoint;

    @Mock
    private Dispatcher dispatcher;

    @Mock
    private DispatcherCache dispatcherCache;

    @Mock
    private SystemUserUtil systemUserUtil;

    @Mock
    private JsonSchemaValidator jsonSchemaValidator;


    @InjectMocks
    private SenderProducer senderProducer;

    @Before
    public void setUp() throws Exception {
        injectionPoint = injectionPointWith(ServiceComponentClassLevelAnnotation.class.getDeclaredField("field"));

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

        final JsonEnvelope envelopeToBeDispatched = envelope()
                .with(metadataOf(id, name).withUserId(userId))
                .withPayloadOf("value1", "someField1")
                .withPayloadOf("value2", "someField2")
                .build();
        final JsonEnvelope expectedResponse = envelope().build();

        when(dispatcher.dispatch(envelopeToBeDispatched)).thenReturn(expectedResponse);

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

        final JsonEnvelope originalEnvelope = envelope()
                .with(metadataWithRandomUUID("some-action"))
                .withPayloadOf("value1", "someField1")
                .withPayloadOf("value2", "someField2")
                .build();
        final JsonEnvelope envelopeWithSysUserId = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope expectedResponse = envelope().build();

        when(systemUserUtil.asEnvelopeWithSystemUserId(originalEnvelope)).thenReturn(envelopeWithSysUserId);
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

        senderProducer.produceSender(injectionPoint).send(envelope()
                .with(metadataWithRandomUUID("some-action"))
                .build());
    }

    @Test
    public void shouldNotThrowExceptionIfPayloadAdheresToJsonSchema() {
        senderProducer.envelopeValidationExceptionHandler = new RethrowingValidationExceptionHandler();

        when(dispatcher.dispatch(any(JsonEnvelope.class)))
                .thenReturn(null);

        senderProducer.produceSender(injectionPoint)
                .send(envelope()
                        .with(metadataWithRandomUUID("some-action"))
                        .withPayloadOf("value1", "someField1")
                        .withPayloadOf("value2", "someField2")
                        .build());
    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class ServiceComponentClassLevelAnnotation {

        @ServiceComponent(COMMAND_HANDLER)
        @Inject
        Object field;
    }
}
