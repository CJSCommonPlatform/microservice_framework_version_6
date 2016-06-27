package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.annotation.Adapter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@ApplicationScoped
public class SynchronousDispatcherProducer {

    @Inject
    DispatcherCache dispatcherCache;

    /**
     * Produces the correct implementation of a synchronous dispatcher depending on the {@link
     * Adapter} annotation at the injection point.
     *
     * @param injectionPoint class where the {@link SynchronousDispatcher} is being injected
     * @return the correct dispatcher instance
     * @throws IllegalStateException if the injection point does not have an {@link Adapter}
     *                               annotation
     */
    @Produces
    public SynchronousDispatcher produceSynchronousDispatcher(final InjectionPoint injectionPoint) {
        return dispatcherCache.dispatcherFor(injectionPoint)::synchronousDispatch;
    }
}
