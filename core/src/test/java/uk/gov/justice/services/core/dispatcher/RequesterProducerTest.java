package uk.gov.justice.services.core.dispatcher;

import static co.unruly.matchers.OptionalMatchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequesterProducerTest {

    @Mock
    InjectionPoint injectionPoint;

    @Mock
    Dispatcher dispatcher;

    @Mock
    DispatcherCache dispatcherCache;

    @Mock
    SystemUserUtil systemUserUtil;


    @InjectMocks
    RequesterProducer requesterProducer;

    @Before
    public void setUp() throws Exception {
        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);

    }

    @Test
    public void shouldReturnRequesterDelegatingToDispatcher() throws Exception {

        final Requester requester = requesterProducer.produceRequester(injectionPoint);
        final UUID id = UUID.randomUUID();
        final String name = "name123";
        final String userId = "usr123";
        final JsonEnvelope envelope = envelope().with(metadataOf(id, name).withUserId(userId)).build();
        requester.request(envelope);

        ArgumentCaptor<JsonEnvelope> dispatchedEnvelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(dispatcher).dispatch(dispatchedEnvelopeCaptor.capture());

        final JsonEnvelope dispatchedEnvelope = dispatchedEnvelopeCaptor.getValue();
        assertThat(envelope, sameInstance(dispatchedEnvelope));
        assertThat(envelope.metadata().id(), is(id));
        assertThat(envelope.metadata().name(), is(name));
        assertThat(envelope.metadata().userId(), contains(userId));

    }

    @Test
    public void requesterShouldDelegateAdminRequestSubstitutingUserId() throws Exception {

        final Requester requester = requesterProducer.produceRequester(injectionPoint);

        final JsonEnvelope originalEnvelope = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope envelopeWithSysUserId = envelope().with(metadataWithDefaults()).build();
        when(systemUserUtil.asEnvelopeWithSystemUserId(originalEnvelope)).thenReturn(envelopeWithSysUserId);
        requester.requestAsAdmin(originalEnvelope);

        verify(dispatcher).dispatch(envelopeWithSysUserId);

    }

}