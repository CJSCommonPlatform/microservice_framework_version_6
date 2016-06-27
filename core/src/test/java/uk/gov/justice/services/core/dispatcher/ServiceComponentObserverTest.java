package uk.gov.justice.services.core.dispatcher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceComponentObserverTest {

    @Mock
    private Dispatcher dispatcher;

    @Mock
    Object handler;

    @Mock
    private BeanManager beanManager;

    @Mock
    private Context context;

    @Mock
    private CreationalContext<Object> creationalContext;

    @Mock
    private Bean<Object> bean;

    @Mock
    private DispatcherCache dispatcherCache;

    @InjectMocks
    private ServiceComponentObserver serviceComponentObserver;

    @Test
    public void shouldRegisterHandler() throws Exception {
        final ServiceComponentFoundEvent foundEvent = new ServiceComponentFoundEvent(COMMAND_API, bean, LOCAL);

        when(dispatcherCache.dispatcherFor(foundEvent)).thenReturn(dispatcher);
        when(beanManager.getContext(bean.getScope())).thenReturn(context);
        when(beanManager.createCreationalContext(bean)).thenReturn(creationalContext);
        when(context.get(bean, creationalContext)).thenReturn(handler);

        serviceComponentObserver.register(foundEvent);

        verify(dispatcher).register(handler);
    }

}