package uk.gov.justice.services.core.interceptor;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.interceptor.exception.InterceptorCacheException;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.enterprise.inject.spi.Bean;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InterceptorCacheTest {

    private static final InterceptorOne INTERCEPTOR_1 = new InterceptorOne();
    private static final InterceptorTwo INTERCEPTOR_2 = new InterceptorTwo();
    private static final InterceptorThree INTERCEPTOR_3 = new InterceptorThree();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private InterceptorChainObserver observer;

    @Mock
    private BeanInstantiater beanInstantiater;

    @InjectMocks
    private InterceptorCache interceptorCache;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnEmptyDequeIfEmptyInterceptorChainProvided() throws Exception {
        givenThreeInterceptorBeans();

        final InterceptorChainProvider interceptorChainProvider = new EmptyInterceptorChainProvider();
        final Bean<InterceptorChainProvider> interceptorChainProviderBean = mock(Bean.class);

        when(observer.getInterceptorChainProviderBeans()).thenReturn(singletonList(interceptorChainProviderBean));
        when(beanInstantiater.instantiate(interceptorChainProviderBean)).thenReturn(interceptorChainProvider);

        interceptorCache.initialise();

        final Deque<Interceptor> interceptors = interceptorCache.getInterceptors("Empty Component");

        assertThat(interceptors, empty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnInstancesOfInterceptorChainTypesInOrderAddedInProvider() throws Exception {
        givenThreeInterceptorBeans();

        final InterceptorChainProvider interceptorChainProvider = new ComponentOneInterceptorChainProvider();
        final Bean<InterceptorChainProvider> interceptorChainProviderBean = mock(Bean.class);

        when(observer.getInterceptorChainProviderBeans()).thenReturn(singletonList(interceptorChainProviderBean));
        when(beanInstantiater.instantiate(interceptorChainProviderBean)).thenReturn(interceptorChainProvider);

        interceptorCache.initialise();

        final Deque<Interceptor> interceptors = interceptorCache.getInterceptors("Component_1");

        assertThat(interceptors, contains(INTERCEPTOR_1, INTERCEPTOR_2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnCorrectInterceptorsForEachComponent() throws Exception {
        givenThreeInterceptorBeans();

        final InterceptorChainProvider interceptorChainProvider_1 = new ComponentOneInterceptorChainProvider();
        final InterceptorChainProvider interceptorChainProvider_2 = new ComponentTwoInterceptorChainProvider();

        final Bean<InterceptorChainProvider> bean_1 = mock(Bean.class);
        final Bean<InterceptorChainProvider> bean_2 = mock(Bean.class);

        when(observer.getInterceptorChainProviderBeans()).thenReturn(asList(bean_1, bean_2));

        when(beanInstantiater.instantiate(bean_1)).thenReturn(interceptorChainProvider_1);
        when(beanInstantiater.instantiate(bean_2)).thenReturn(interceptorChainProvider_2);

        interceptorCache.initialise();

        final Deque<Interceptor> interceptors_1 = interceptorCache.getInterceptors("Component_1");
        final Deque<Interceptor> interceptors_2 = interceptorCache.getInterceptors("Component_2");

        assertThat(interceptors_1, contains(INTERCEPTOR_1, INTERCEPTOR_2));
        assertThat(interceptors_2, contains(INTERCEPTOR_2, INTERCEPTOR_3));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCombineInterceptorsFromMultipleProvidersForComponentAndOrderInPriority() throws Exception {
        givenThreeInterceptorBeans();

        final InterceptorChainProvider interceptorChainProvider = new ComponentOneInterceptorChainProvider();
        final InterceptorChainProvider interceptorChainProviderExtra = new ComponentOneExtraInterceptorChainProvider();

        final Bean<InterceptorChainProvider> interceptorChainProviderBean_1 = mock(Bean.class);
        final Bean<InterceptorChainProvider> interceptorChainProviderBean_2 = mock(Bean.class);

        when(observer.getInterceptorChainProviderBeans()).thenReturn(asList(interceptorChainProviderBean_1, interceptorChainProviderBean_2));

        when(beanInstantiater.instantiate(interceptorChainProviderBean_1)).thenReturn(interceptorChainProvider);
        when(beanInstantiater.instantiate(interceptorChainProviderBean_2)).thenReturn(interceptorChainProviderExtra);

        interceptorCache.initialise();

        final Deque<Interceptor> interceptors = interceptorCache.getInterceptors("Component_1");

        assertThat(interceptors, contains(INTERCEPTOR_1, INTERCEPTOR_3, INTERCEPTOR_2));
    }

    @Test
    public void shouldThrowExceptionIfComponentHasNoInterceptorChainProviderRegistered() throws Exception {
        expectedException.expect(InterceptorCacheException.class);
        expectedException.expectMessage("Component [Unknown Component] does not have any cached Interceptors, check if there is an InterceptorChainProvider for this component.");

        interceptorCache.getInterceptors("Unknown Component");
    }

    @Test
    public void shouldThrowExceptionIfInterceptorBeanNotInstantiated() throws Exception {
        expectedException.expect(InterceptorCacheException.class);
        expectedException.expectMessage("Could not instantiate interceptor bean of type: uk.gov.justice.services.core.interceptor.InterceptorCacheTest$InterceptorOne");

        final Class interceptorClass_1 = InterceptorOne.class;


        final Bean<Interceptor> interceptorBean_1 = mock(Bean.class);
        when(observer.getInterceptorBeans()).thenReturn(asList(interceptorBean_1));

        when(interceptorBean_1.getBeanClass()).thenReturn(interceptorClass_1);
        when(beanInstantiater.instantiate(interceptorBean_1)).thenReturn(null);
        final Bean<InterceptorChainProvider> interceptorChainProviderBean = mock(Bean.class);
        when(observer.getInterceptorChainProviderBeans()).thenReturn(singletonList(interceptorChainProviderBean));

        when(beanInstantiater.instantiate(interceptorChainProviderBean)).thenReturn(new InterceptorChainProvider() {
            @Override
            public String component() {
                return "some component";
            }

            @Override
            public List<InterceptorChainEntry> interceptorChainTypes() {
                final List<InterceptorChainEntry> interceptorChainTypes = new ArrayList<>();
                interceptorChainTypes.add(new InterceptorChainEntry(1, interceptorClass_1));
                return interceptorChainTypes;
            }
        });

        interceptorCache.initialise();


    }

    @SuppressWarnings("unchecked")
    private void givenThreeInterceptorBeans() {
        final Bean<Interceptor> interceptorBean_1 = mock(Bean.class);
        final Bean<Interceptor> interceptorBean_2 = mock(Bean.class);
        final Bean<Interceptor> interceptorBean_3 = mock(Bean.class);

        final Class interceptorClass_1 = InterceptorOne.class;
        final Class interceptorClass_2 = InterceptorTwo.class;
        final Class interceptorClass_3 = InterceptorThree.class;

        when(observer.getInterceptorBeans()).thenReturn(asList(interceptorBean_1, interceptorBean_2, interceptorBean_3));

        when(interceptorBean_1.getBeanClass()).thenReturn(interceptorClass_1);
        when(interceptorBean_2.getBeanClass()).thenReturn(interceptorClass_2);
        when(interceptorBean_3.getBeanClass()).thenReturn(interceptorClass_3);

        when(beanInstantiater.instantiate(interceptorBean_1)).thenReturn(INTERCEPTOR_1);
        when(beanInstantiater.instantiate(interceptorBean_2)).thenReturn(INTERCEPTOR_2);
        when(beanInstantiater.instantiate(interceptorBean_3)).thenReturn(INTERCEPTOR_3);
    }

    public class EmptyInterceptorChainProvider implements InterceptorChainProvider {

        @Override
        public String component() {
            return "Empty Component";
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            return emptyList();
        }
    }

    public class ComponentOneInterceptorChainProvider implements InterceptorChainProvider {

        @Override
        public String component() {
            return "Component_1";
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            final List<InterceptorChainEntry> interceptorChainTypes = new ArrayList<>();
            interceptorChainTypes.add(new InterceptorChainEntry(1, InterceptorOne.class));
            interceptorChainTypes.add(new InterceptorChainEntry(3, InterceptorTwo.class));
            return interceptorChainTypes;
        }
    }

    public class ComponentTwoInterceptorChainProvider implements InterceptorChainProvider {

        @Override
        public String component() {
            return "Component_2";
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            final List<InterceptorChainEntry> interceptorChainTypes = new ArrayList<>();
            interceptorChainTypes.add(new InterceptorChainEntry(1, InterceptorTwo.class));
            interceptorChainTypes.add(new InterceptorChainEntry(2, InterceptorThree.class));
            return interceptorChainTypes;
        }
    }

    public class ComponentOneExtraInterceptorChainProvider implements InterceptorChainProvider {

        @Override
        public String component() {
            return "Component_1";
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            final List<InterceptorChainEntry> interceptorChainTypes = new ArrayList<>();
            interceptorChainTypes.add(new InterceptorChainEntry(2, InterceptorThree.class));
            return interceptorChainTypes;
        }
    }

    public static class InterceptorOne implements Interceptor {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
            return interceptorChain.processNext(interceptorContext);
        }
    }

    public static class InterceptorTwo implements Interceptor {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
            return interceptorChain.processNext(interceptorContext);
        }
    }

    public static class InterceptorThree implements Interceptor {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
            return interceptorChain.processNext(interceptorContext);
        }
    }
}
