package uk.gov.justice.services.core.interceptor;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

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
        final InterceptorChainEntryProvider interceptorChainEntryProvider = getInterceptorChainEntryProvider(providerBean);

        final Set<InterceptorChainEntryInstance> interceptors = newOrCachedInterceptorChain(interceptorChainEntryProvider, orderedComponentInterceptors);

        interceptorChainEntryProvider.interceptorChainTypes().forEach(chainEntry -> {

            final Integer priority = chainEntry.getPriority();
            interceptors.add(new InterceptorChainEntryInstance(priority, interceptorInstanceFrom(interceptorInstancesByType, chainEntry.getInterceptorType())));
        });

        orderedComponentInterceptors.put(interceptorChainEntryProvider.component(), interceptors);
    }

    private InterceptorChainEntryProvider getInterceptorChainEntryProvider(final Bean<?> providerBean) {
        final Object provider = beanInstantiater.instantiate(providerBean);

        if (provider instanceof InterceptorChainProvider) {
            return new ConvertedInterceptorChainEntryProvider((InterceptorChainProvider) provider);
        }

        return (InterceptorChainEntryProvider) provider;
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

    private Set<InterceptorChainEntryInstance> newOrCachedInterceptorChain(final InterceptorChainEntryProvider interceptorChainEntryProvider,
                                                                           final HashMap<String, Set<InterceptorChainEntryInstance>> orderedComponentInterceptors) {
        if (orderedComponentInterceptors.containsKey(interceptorChainEntryProvider.component())) {
            return orderedComponentInterceptors.get(interceptorChainEntryProvider.component());
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

    private class ConvertedInterceptorChainEntryProvider implements InterceptorChainEntryProvider {

        private final String component;
        private final List<InterceptorChainEntry> interceptorChainTypes;

        public ConvertedInterceptorChainEntryProvider(final InterceptorChainProvider interceptorChainProvider) {
            this.component = interceptorChainProvider.component();

            this.interceptorChainTypes = interceptorChainProvider.interceptorChainTypes().stream()
                    .map(integerClassPair -> new InterceptorChainEntry(integerClassPair.getKey(), integerClassPair.getValue()))
                    .collect(toList());
        }

        @Override
        public String component() {
            return component;
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            return interceptorChainTypes;
        }
    }
}
