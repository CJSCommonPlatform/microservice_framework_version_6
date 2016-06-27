package uk.gov.justice.services.core.dispatcher;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
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

    @InjectMocks
    RequesterProducer dispatcherProducer;

    @Test
    public void shouldReturnDispatcher() throws Exception {
        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        Requester result = dispatcherProducer.produceRequester(injectionPoint);
        assertThat(result, notNullValue());
    }

}