package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InterceptorChainObserverTest {

    @InjectMocks
    private InterceptorChainObserver interceptorChainObserver;

    @Test
    public void shouldRegisterInterceptorChainEntryProvider() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final Set<Bean<?>> beans = new HashSet<>();
        beans.add(bean_1);
        beans.add(bean_2);

        when(beanManager.getBeans(eq(InterceptorChainEntryProvider.class), any(AnnotationLiteral.class))).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(Object.class);
        when(bean_2.getBeanClass()).thenReturn(Object.class);

        interceptorChainObserver.afterDeploymentValidation(event, beanManager);

        final List<Bean<?>> interceptorBeans = interceptorChainObserver.getInterceptorChainProviderBeans();

        assertThat(interceptorBeans, containsInAnyOrder(bean_1, bean_2));
    }

    @Test
    public void shouldRegisterDeprecatedInterceptorChainProvider() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final Set<Bean<?>> beans = new HashSet<>();
        beans.add(bean_1);
        beans.add(bean_2);

        when(beanManager.getBeans(eq(InterceptorChainProvider.class), any(AnnotationLiteral.class))).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(Object.class);
        when(bean_2.getBeanClass()).thenReturn(Object.class);

        interceptorChainObserver.afterDeploymentValidation(event, beanManager);

        final List<Bean<?>> interceptorBeans = interceptorChainObserver.getInterceptorChainProviderBeans();

        assertThat(interceptorBeans, containsInAnyOrder(bean_1, bean_2));
    }

    @Test
    public void shouldRegisterBothTypesOfInterceptorChainProvider() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final Set<Bean<?>> interceptorChainEntryProviderBeans = new HashSet<>();
        interceptorChainEntryProviderBeans.add(bean_1);

        final Set<Bean<?>> interceptorChainProviderBeans = new HashSet<>();
        interceptorChainProviderBeans.add(bean_2);

        when(beanManager.getBeans(eq(InterceptorChainEntryProvider.class), any(AnnotationLiteral.class))).thenReturn(interceptorChainEntryProviderBeans);
        when(bean_1.getBeanClass()).thenReturn(Object.class);

        when(beanManager.getBeans(eq(InterceptorChainProvider.class), any(AnnotationLiteral.class))).thenReturn(interceptorChainProviderBeans);
        when(bean_2.getBeanClass()).thenReturn(Object.class);

        interceptorChainObserver.afterDeploymentValidation(event, beanManager);

        final List<Bean<?>> interceptorBeans = interceptorChainObserver.getInterceptorChainProviderBeans();

        assertThat(interceptorBeans, containsInAnyOrder(bean_1, bean_2));
    }

    @Test
    public void shouldRegisterInterceptor() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final Set<Bean<?>> beans = new HashSet<>();
        beans.add(bean_1);
        beans.add(bean_2);

        when(beanManager.getBeans(eq(Interceptor.class), any(AnnotationLiteral.class))).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(Object.class);
        when(bean_2.getBeanClass()).thenReturn(Object.class);

        interceptorChainObserver.afterDeploymentValidation(event, beanManager);

        final List<Bean<?>> interceptorBeans = interceptorChainObserver.getInterceptorBeans();

        assertThat(interceptorBeans, containsInAnyOrder(bean_1, bean_2));
    }
}