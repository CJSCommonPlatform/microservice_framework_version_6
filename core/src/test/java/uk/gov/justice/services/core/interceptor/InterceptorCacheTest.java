package uk.gov.justice.services.core.interceptor;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.extension.BeanInstantiater;

import java.util.Deque;
import java.util.List;

import javax.enterprise.inject.spi.Bean;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InterceptorCacheTest {

    @Mock
    InterceptorObserver observer;

    @Mock
    BeanInstantiater beanInstantiater;

    @InjectMocks
    InterceptorCache interceptorCache;

    @Test
    public void shouldReturnEmptyInterceptorsList() throws Exception {
        assertThat(interceptorCache.getInterceptors().isEmpty(), is(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnAddedInterceptorsInPriorityOrderAsList() throws Exception {

        final Interceptor interceptor_1 = mock(Interceptor.class);
        final Interceptor interceptor_2 = mock(Interceptor.class);
        final Interceptor interceptor_3 = mock(Interceptor.class);
        final Interceptor interceptor_4 = mock(Interceptor.class);

        final Bean<Interceptor> bean_1 = mock(Bean.class);
        final Bean<Interceptor> bean_2 = mock(Bean.class);
        final Bean<Interceptor> bean_3 = mock(Bean.class);
        final Bean<Interceptor> bean_4 = mock(Bean.class);

        final List<Bean<?>> beans = asList(bean_1, bean_2, bean_3, bean_4);

        when(observer.getInterceptorBeans()).thenReturn(beans);
        when(beanInstantiater.instantiate(bean_1)).thenReturn(interceptor_1);
        when(beanInstantiater.instantiate(bean_2)).thenReturn(interceptor_2);
        when(beanInstantiater.instantiate(bean_3)).thenReturn(interceptor_3);
        when(beanInstantiater.instantiate(bean_4)).thenReturn(interceptor_4);

        when(interceptor_1.priority()).thenReturn(3);
        when(interceptor_2.priority()).thenReturn(2);
        when(interceptor_3.priority()).thenReturn(4);
        when(interceptor_4.priority()).thenReturn(1);

        interceptorCache.initialise();

        final Deque<Interceptor> interceptors = interceptorCache.getInterceptors();

        assertThat(interceptors, Matchers.contains(interceptor_4, interceptor_2, interceptor_1, interceptor_3));
    }
}
