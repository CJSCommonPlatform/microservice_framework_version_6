package uk.gov.justice.services.management.shuttering.observers;

import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.UNSHUTTERED;

import uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

@Singleton
public class ShutteringStateRegistry {

    private final AtomicReference<ContextShutteredState> shutteredState = new AtomicReference<>(UNSHUTTERED);

    public void setShutteredState(final  ContextShutteredState contextShutteredState) {
        shutteredState.set(contextShutteredState);
    }

    public ContextShutteredState getShutteredState() {
        return shutteredState.get();
    }
}
