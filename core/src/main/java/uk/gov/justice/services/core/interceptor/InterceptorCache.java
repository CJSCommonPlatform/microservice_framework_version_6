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
        final HashMap<String, Set<InterceptorChainEntryInstance>> orderedComponentInterceptors = new HashMap<>();
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

        throw new InterceptorCacheException(format("Component [%s] does not have any cached Interceptors, check if there is an InterceptorChainProvider for this component.", component));
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
                                                    final HashMap<String, Set<InterceptorChainEntryInstance>> orderedComponentInterceptors,
                                                    final Bean<?> providerBean) {
        final InterceptorChainProvider interceptorChainProvider = (InterceptorChainProvider) beanInstantiater.instantiate(providerBean);

        final Set<InterceptorChainEntryInstance> interceptors = newOrCachedInterceptorChain(interceptorChainProvider, orderedComponentInterceptors);

        interceptorChainProvider.interceptorChainTypes().forEach(chainEntry -> {

            final Integer priority = chainEntry.getPriority();
            interceptors.add(new InterceptorChainEntryInstance(priority, interceptorInstanceFrom(interceptorInstancesByType, chainEntry.getInterceptorType())));
        });

        orderedComponentInterceptors.put(interceptorChainProvider.component(), interceptors);
    }

    private Interceptor interceptorInstanceFrom(final Map<Class<?>, Interceptor> interceptorInstancesByType, final Class<? extends Interceptor> interceptorChainType) {
        final Interceptor interceptorInstance = interceptorInstancesByType.get(interceptorChainType);
        if (interceptorInstance == null) {
            throw new InterceptorCacheException(format("Could not instantiate interceptor bean of type: %s", interceptorChainType.getName()));
        }
        return interceptorInstance;
    }

    private void createComponentInterceptorsFrom(final HashMap<String, Set<InterceptorChainEntryInstance>> orderedComponentInterceptors) {
        orderedComponentInterceptors.forEach((key, value) -> {
            final Deque<Interceptor> interceptors = value.stream()
                    .map(InterceptorChainEntryInstance::getInterceptor)
                    .collect(toCollection(LinkedList::new));

            componentInterceptors.put(key, interceptors);
        });
    }

    private Set<InterceptorChainEntryInstance> newOrCachedInterceptorChain(final InterceptorChainProvider interceptorChainProvider,
                                                                           final HashMap<String, Set<InterceptorChainEntryInstance>> orderedComponentInterceptors) {
        if (orderedComponentInterceptors.containsKey(interceptorChainProvider.component())) {
            return orderedComponentInterceptors.get(interceptorChainProvider.component());
        }

        return new TreeSet<>(comparing(InterceptorChainEntryInstance::getPriority));
    }

    private class InterceptorChainEntryInstance {

        private final Integer priority;
        private final Interceptor interceptor;

        InterceptorChainEntryInstance(final Integer priority, final Interceptor interceptor) {
            this.priority = priority;
            this.interceptor = interceptor;
        }

        public Integer getPriority() {
            return priority;
        }

        public Interceptor getInterceptor() {
            return interceptor;
        }
    }
}
