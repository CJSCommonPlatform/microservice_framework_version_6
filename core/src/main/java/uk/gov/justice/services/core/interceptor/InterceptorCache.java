package uk.gov.justice.services.core.interceptor;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.interceptor.exception.InterceptorCacheException;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


/**
 * Handles the adding of {@link Interceptor} to a master queue and creating {@link
 * InterceptorChainProcessor}.
 */
@ApplicationScoped
public class InterceptorCache {

    private final HashMap<String, Deque<Interceptor>> componentInterceptors = new HashMap<>();

    @Inject
    InterceptorChainObserver interceptorChainObserver;

    @Inject
    BeanInstantiater beanInstantiater;

    @PostConstruct
    public void initialise() {
        final HashMap<String, Set<Pair<Integer, Interceptor>>> orderedComponentInterceptors = new HashMap<>();
        final Map<Class<?>, Interceptor> interceptorInstancesByType = interceptorInstancesByType();

        interceptorChainObserver.getInterceptorChainProviderBeans().forEach(providerBean ->
                createInterceptorChainsByComponent(interceptorInstancesByType, orderedComponentInterceptors, providerBean));

        createComponentInterceptorsFrom(orderedComponentInterceptors);
    }

    /**
     * Get the deque of interceptors in priority order as a new deque.  Priority of low is highest.
     * e.g. 1 = Highest Priority
     *
     * @return a deque of interceptors in priority order
     */
    Deque<Interceptor> getInterceptors(final String component) {
        if (componentInterceptors.containsKey(component)) {
            return new LinkedList<>(componentInterceptors.get(component));
        }

        throw new InterceptorCacheException(format("Component [%s] does not have any cached Interceptors, check there is an InterceptorChainProvider for this component.", component));
    }

    private Map<Class<?>, Interceptor> interceptorInstancesByType() {
        final List<Bean<?>> interceptorBeans = interceptorChainObserver.getInterceptorBeans();
        final Map<Class<?>, Interceptor> interceptorInstancesByType = new HashMap<>();

        interceptorBeans.forEach(interceptorBean -> {
            final Class<?> interceptorType = interceptorBean.getBeanClass();
            final Object interceptorInstance = beanInstantiater.instantiate(interceptorBean);

            interceptorInstancesByType.put(interceptorType, (Interceptor) interceptorInstance);
        });

        return interceptorInstancesByType;
    }

    private void createInterceptorChainsByComponent(final Map<Class<?>, Interceptor> interceptorInstancesByType,
                                                    final HashMap<String, Set<Pair<Integer, Interceptor>>> orderedComponentInterceptors,
                                                    final Bean<?> providerBean) {
        final InterceptorChainProvider interceptorChainProvider = (InterceptorChainProvider) beanInstantiater.instantiate(providerBean);

        final Set<Pair<Integer, Interceptor>> interceptors = newOrCachedInterceptorChain(interceptorChainProvider, orderedComponentInterceptors);

        interceptorChainProvider.interceptorChainTypes().forEach(interceptorChainType -> {

            final Integer priority = interceptorChainType.getLeft();
            interceptors.add(new ImmutablePair<>(priority, interceptorInstanceFrom(interceptorInstancesByType, interceptorChainType)));
        });

        orderedComponentInterceptors.put(interceptorChainProvider.component(), interceptors);
    }

    private Interceptor interceptorInstanceFrom(final Map<Class<?>, Interceptor> interceptorInstancesByType, final Pair<Integer, Class<? extends Interceptor>> interceptorChainType) {
        final Interceptor interceptorInstance = interceptorInstancesByType.get(interceptorChainType.getRight());
        if (interceptorInstance == null) {
            throw new InterceptorCacheException(format("Could not instantiate interceptor bean of type: %s", interceptorChainType.getRight().getName()));
        }
        return interceptorInstance;
    }

    private void createComponentInterceptorsFrom(final HashMap<String, Set<Pair<Integer, Interceptor>>> orderedComponentInterceptors) {
        orderedComponentInterceptors.forEach((key, value) -> {
            final Deque<Interceptor> interceptors = value.stream()
                    .map(Pair::getRight)
                    .collect(toCollection(LinkedList::new));

            componentInterceptors.put(key, interceptors);
        });
    }

    private Set<Pair<Integer, Interceptor>> newOrCachedInterceptorChain(final InterceptorChainProvider interceptorChainProvider,
                                                                        final HashMap<String, Set<Pair<Integer, Interceptor>>> orderedComponentInterceptors) {
        if (orderedComponentInterceptors.containsKey(interceptorChainProvider.component())) {
            return orderedComponentInterceptors.get(interceptorChainProvider.component());
        }

        return new TreeSet<>(comparing(Pair::getLeft));
    }
}