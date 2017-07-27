package uk.gov.justice.services.core.requester;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationException;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandler;
import uk.gov.justice.services.core.envelope.RethrowingValidationExceptionHandler;
import uk.gov.justice.services.core.json.DefaultJsonSchemaValidator;
import uk.gov.justice.services.messaging.JsonEnvelope;

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

        final JsonEnvelope envelopeToBeDispatched = mock(JsonEnvelope.class);
        final JsonEnvelope expectedResponse = mock(JsonEnvelope.class);

        when(dispatcher.dispatch(envelopeToBeDispatched)).thenReturn(expectedResponse);

        final JsonEnvelope returnedResponse = requester.request(envelopeToBeDispatched);

        ArgumentCaptor<JsonEnvelope> dispatchedEnvelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(dispatcher).dispatch(dispatchedEnvelopeCaptor.capture());

        final JsonEnvelope dispatchedEnvelope = dispatchedEnvelopeCaptor.getValue();
        assertThat(envelopeToBeDispatched, sameInstance(dispatchedEnvelope));
        assertThat(returnedResponse, is(expectedResponse));

    }

    @Test
    public void requesterShouldDelegateAdminRequestSubstitutingUserId() throws Exception {

        final Requester requester = requesterProducer.produceRequester(injectionPoint);

        final JsonEnvelope originalEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope envelopeWithSysUserId = mock(JsonEnvelope.class);
        final JsonEnvelope expectedResponse = mock(JsonEnvelope.class);

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
                .thenReturn(envelopeFrom(
                        metadataBuilder().withId(randomUUID()).withName("some-action"),
                        createObjectBuilder().add("someField1", "value1")));

        requesterProducer.produceRequester(injectionPoint).request(mock(JsonEnvelope.class));
    }

    @Test
    public void shouldNotThrowExceptionIfResponsePayloadAdheresToJsonSchema() {
        requesterProducer.envelopeValidationExceptionHandler = new RethrowingValidationExceptionHandler();

        when(dispatcher.dispatch(any(JsonEnvelope.class)))
                .thenReturn(envelopeFrom(
                        metadataBuilder().withId(randomUUID()).withName("some-action"),
                        createObjectBuilder()
                                .add("someField1", "value1")
                                .add("someField2", "value2")));

        requesterProducer.produceRequester(injectionPoint).request(mock(JsonEnvelope.class));

    }

}
