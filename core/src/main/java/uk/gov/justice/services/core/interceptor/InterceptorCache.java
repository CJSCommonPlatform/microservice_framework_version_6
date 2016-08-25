package uk.gov.justice.services.core.interceptor;

import static java.util.Comparator.comparing;

import uk.gov.justice.services.core.extension.BeanInstantiater;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Handles the adding of {@link Interceptor} to a master queue and creating {@link
 * InterceptorChainProcessor}.
 */
@ApplicationScoped
public class InterceptorCache {

    private final Set<Interceptor> interceptors = new TreeSet<>(comparing(Interceptor::priority));

    @Inject
    InterceptorObserver observer;

    @Inject
    BeanInstantiater beanInstantiater;

    @PostConstruct
    public void initialise() {
        observer.getInterceptorBeans().forEach(bean -> interceptors.add((Interceptor) beanInstantiater.instantiate(bean)));
    }

    /**
     * Get the deque of interceptors in priority order as a new deque.
     *
     * @return a deque of interceptors in priority order
     */
    public Deque<Interceptor> getInterceptors() {
        return new LinkedList<>(interceptors);
    }
}