package uk.gov.justice.services.messaging.jms;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.system.domain.StoredCommand;
import uk.gov.justice.services.system.persistence.StoredCommandRepository;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ShutteringStoreSenderTest {

    @Mock
    private StoredCommandRepository storedCommandRepository;

    @Mock
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private ShutteringStoreSender shutteringStoreSender;

    @Captor
    private ArgumentCaptor<StoredCommand> shutteredCommandCaptor;

    @Test
    public void shouldConvertToShutteredCommandAndSave() throws Exception {

        final String destinationName = "destinationName";
        final String envelopeJson = "envelope json";
        final ZonedDateTime now = new UtcClock().now();

        final UUID envelopeId = randomUUID();

        final JsonEnvelope command = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        when(command.metadata()).thenReturn(metadata);
        when(metadata.id()).thenReturn(envelopeId);
        when(jsonObjectEnvelopeConverter.asJsonString(command)).thenReturn(envelopeJson);
        when(clock.now()).thenReturn(now);

        shutteringStoreSender.send(command, destinationName);

        verify(storedCommandRepository).save(shutteredCommandCaptor.capture());

        final StoredCommand storedCommand = shutteredCommandCaptor.getValue();

        assertThat(storedCommand.getEnvelopeId(), is(envelopeId));
        assertThat(storedCommand.getCommandJsonEnvelope(), is(envelopeJson));
        assertThat(storedCommand.getDestination(), is(destinationName));
        assertThat(storedCommand.getDateReceived(), is(now));
    }
}
