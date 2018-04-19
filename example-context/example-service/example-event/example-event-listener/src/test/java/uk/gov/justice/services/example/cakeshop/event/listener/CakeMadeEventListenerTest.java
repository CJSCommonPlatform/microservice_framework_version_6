package uk.gov.justice.services.example.cakeshop.event.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.example.cakeshop.persistence.CakeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Cake;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CakeMadeEventListenerTest {

    @Mock
    private CakeRepository cakeRepository;

    @Mock
    private JsonObjectToObjectConverter converter;

    @InjectMocks
    private CakeMadeEventListener cakeMadeEventListener = new CakeMadeEventListener();

    @Test
    public void shouldSaveCake() {
        final Envelope<Cake> envelope = mock(Envelope.class);
        final Cake cake = mock(Cake.class);
        when(envelope.payload()).thenReturn(cake);
        final Metadata metadata = mock(Metadata.class);
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.streamId()).thenReturn(Optional.empty());
        cakeMadeEventListener.handle(envelope);

        verify(cakeRepository).save(cake);
    }

}
