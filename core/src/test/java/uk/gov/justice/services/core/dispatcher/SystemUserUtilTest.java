package uk.gov.justice.services.core.dispatcher;

import static co.unruly.matchers.OptionalMatchers.contains;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SystemUserUtilTest {

    @Mock
    private SystemUserProvider systemUserProvider;

    @InjectMocks
    private SystemUserUtil systemUserUtil;

    @Test
    public void shouldReturnEnvelopeWithSystemUserId() throws Exception {

        final UUID sysUserId = randomUUID();
        when(systemUserProvider.getContextSystemUserId()).thenReturn(Optional.of(sysUserId));

        final UUID id = UUID.randomUUID();
        final String name = "name456";

        final JsonEnvelope returnedEnvelope = systemUserUtil.asEnvelopeWithSystemUserId(
                envelope()
                        .with(metadataOf(id, name).withUserId("someIdToBeReplaced"))
                        .withPayloadOf(BigDecimal.valueOf(123), "numName")
                        .build());


        assertThat(returnedEnvelope.metadata().id(), is(id));
        assertThat(returnedEnvelope.metadata().name(), is(name));
        assertThat(returnedEnvelope.metadata().userId(), contains(sysUserId.toString()));
        assertThat(returnedEnvelope.payloadAsJsonObject().getInt("numName"), is(123));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfSystemUserNotFound() throws Exception {
        when(systemUserProvider.getContextSystemUserId()).thenReturn(Optional.empty());

        systemUserUtil.asEnvelopeWithSystemUserId(envelope().with(metadataWithDefaults()).build());
    }
}