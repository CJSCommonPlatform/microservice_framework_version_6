package uk.gov.justice.services.example.cakeshop.event.listener;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.example.cakeshop.persistence.CakeOrderRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.CakeOrder;
import uk.gov.justice.services.messaging.Envelope;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CakeOrderedEventListenerTest {

    @Mock
    private CakeOrderRepository repository;

    @Mock
    private JsonObjectToObjectConverter converter;

    @InjectMocks
    private CakeOrderedEventListener listener;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldSaveEvent() throws Exception {

        final Envelope<CakeOrder> envelope = mock(Envelope.class);
        final CakeOrder cakeOrderObject = new CakeOrder(UUID.randomUUID(), UUID.randomUUID(), ZonedDateTime.now());
        when(envelope.payload()).thenReturn(cakeOrderObject);

        listener.handle(envelope);

        verify(repository).save(cakeOrderObject);

    }
}
