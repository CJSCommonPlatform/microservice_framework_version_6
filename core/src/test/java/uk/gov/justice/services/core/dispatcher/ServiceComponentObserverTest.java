package uk.gov.justice.services.core.dispatcher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import javax.enterprise.inject.spi.Bean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceComponentObserverTest {

    @Mock
    Object handler;

    @Mock
    private Dispatcher dispatcher;

    @Mock
    private BeanInstantiater beanInstantiater;

    @Mock
    private Bean<Object> bean;

    @Mock
    private DispatcherCache dispatcherCache;

    @InjectMocks
    private ServiceComponentObserver serviceComponentObserver;

    @Test
    public void shouldRegisterHandler() throws Exception {
        final ServiceComponentFoundEvent foundEvent = new ServiceComponentFoundEvent("COMMAND_API", bean, LOCAL);

        when(dispatcherCache.dispatcherFor(foundEvent)).thenReturn(dispatcher);
        when(beanInstantiater.instantiate(bean)).thenReturn(handler);

        serviceComponentObserver.register(foundEvent);

        verify(dispatcher).register(handler);
    }
}