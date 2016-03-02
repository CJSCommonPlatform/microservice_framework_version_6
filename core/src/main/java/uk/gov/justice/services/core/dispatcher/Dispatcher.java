package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.messaging.Envelope;

/**
 * Dispatches command to the correct handler.
 * The framework will inject the correct implementation based on the {@link Adapter} annotation.
 */
@FunctionalInterface
public interface Dispatcher {

    /**
     * Dispatches the {@code envelope} to the correct handler.
     *
     * @param envelope The {@link Envelope} to be dispatched.
     */
    void dispatch(final Envelope envelope);
}
