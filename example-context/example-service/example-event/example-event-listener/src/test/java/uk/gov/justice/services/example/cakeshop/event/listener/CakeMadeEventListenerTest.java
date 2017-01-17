package uk.gov.justice.services.example.cakeshop.event.listener;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.example.cakeshop.persistence.CakeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Cake;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
        final JsonEnvelope envelope = Mockito.mock(JsonEnvelope.class);
        final Cake cake = Mockito.mock(Cake.class);
        given(converter.convert(envelope.payloadAsJsonObject(), Cake.class)).willReturn(cake);

        cakeMadeEventListener.handle(envelope);

        verify(cakeRepository).save(cake);
    }

}