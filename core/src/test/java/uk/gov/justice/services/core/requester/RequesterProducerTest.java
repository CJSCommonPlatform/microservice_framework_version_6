package uk.gov.justice.services.core.requester;

import static co.unruly.matchers.OptionalMatchers.contains;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationException;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandler;
import uk.gov.justice.services.core.envelope.RethrowingValidationExceptionHandler;
import uk.gov.justice.services.core.json.DefaultJsonSchemaValidator;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.requester.RequesterProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.enterprise.inject.spi.InjectionPoint;

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
public class RequesterProducerTest {

    @Mock
    private InjectionPoint injectionPoint;

    @Mock
    private Dispatcher dispatcher;

    @Mock
    private DispatcherCache dispatcherCache;

    @Mock
    private SystemUserUtil systemUserUtil;

    @Mock
    private EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;

    @InjectMocks
    private RequesterProducer requesterProducer;

    @Before
    public void setUp() throws Exception {
        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        requesterProducer.jsonSchemaValidator = new DefaultJsonSchemaValidator();
        requesterProducer.objectMapper = new ObjectMapperProducer().objectMapper();
    }

    @Test
    public void shouldReturnRequesterDelegatingToDispatcher() throws Exception {

        final Requester requester = requesterProducer.produceRequester(injectionPoint);
        final UUID id = randomUUID();
        final String name = "name123";
        final String userId = "usr123";

        final JsonEnvelope envelopeToBeDispatched = envelope().with(metadataOf(id, name).withUserId(userId)).build();
        final JsonEnvelope expectedResponse = envelope().build();

        when(dispatcher.dispatch(envelopeToBeDispatched)).thenReturn(expectedResponse);

        final JsonEnvelope returnedResponse = requester.request(envelopeToBeDispatched);

        ArgumentCaptor<JsonEnvelope> dispatchedEnvelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(dispatcher).dispatch(dispatchedEnvelopeCaptor.capture());

        final JsonEnvelope dispatchedEnvelope = dispatchedEnvelopeCaptor.getValue();
        assertThat(envelopeToBeDispatched, sameInstance(dispatchedEnvelope));
        assertThat(dispatchedEnvelope.metadata().id(), is(id));
        assertThat(dispatchedEnvelope.metadata().name(), is(name));
        assertThat(dispatchedEnvelope.metadata().userId(), contains(userId));
        assertThat(returnedResponse, is(expectedResponse));

    }

    @Test
    public void requesterShouldDelegateAdminRequestSubstitutingUserId() throws Exception {

        final Requester requester = requesterProducer.produceRequester(injectionPoint);

        final JsonEnvelope originalEnvelope = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope envelopeWithSysUserId = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope expectedResponse = envelope().build();

        when(systemUserUtil.asEnvelopeWithSystemUserId(originalEnvelope)).thenReturn(envelopeWithSysUserId);
        when(dispatcher.dispatch(envelopeWithSysUserId)).thenReturn(expectedResponse);

        final JsonEnvelope returnedResponse = requester.requestAsAdmin(originalEnvelope);

        verify(dispatcher).dispatch(envelopeWithSysUserId);
        assertThat(returnedResponse, is(expectedResponse));

    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfResponseNotValidAgainstSchema() throws Exception {
        requesterProducer.envelopeValidationExceptionHandler = new RethrowingValidationExceptionHandler();

        exception.expect(EnvelopeValidationException.class);
        exception.expectMessage("Message not valid against schema");

        when(dispatcher.dispatch(any(JsonEnvelope.class)))
                .thenReturn(envelope()
                        .with(metadataWithRandomUUID("some-action"))
                        .withPayloadOf("value1", "someField1")
                        .build());

        requesterProducer.produceRequester(injectionPoint).request(envelope().build());
    }

    @Test
    public void shouldNotThrowExceptionIfResponsePayloadAdheresToJsonSchema() {
        requesterProducer.envelopeValidationExceptionHandler = new RethrowingValidationExceptionHandler();

        when(dispatcher.dispatch(any(JsonEnvelope.class)))
                .thenReturn(envelope()
                        .with(metadataWithRandomUUID("some-action"))
                        .withPayloadOf("value1", "someField1")
                        .withPayloadOf("value2", "someField2")
                        .build());

        requesterProducer.produceRequester(injectionPoint).request(envelope().build());

    }

}
