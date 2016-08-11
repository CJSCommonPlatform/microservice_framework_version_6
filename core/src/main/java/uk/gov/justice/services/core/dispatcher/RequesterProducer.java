package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.annotation.ServiceComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@ApplicationScoped
public class RequesterProducer {

    @Inject
    DispatcherCache dispatcherCache;

    /**
     * Produces the correct implementation of a requester depending on the {@link ServiceComponent}
     * annotation at the injection point.
     *
     * @param injectionPoint class where the {@link Requester} is being injected
     * @return the correct requester instance
     * @throws IllegalStateException if the injection point does not have a {@link ServiceComponent}
     *                               annotation
     */
    @Produces
    public Requester produceRequester(final InjectionPoint injectionPoint) {
        return dispatcherCache.dispatcherFor(injectionPoint)::dispatch;
    }
}
