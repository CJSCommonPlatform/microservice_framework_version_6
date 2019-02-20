package uk.gov.justice.services.core.lifecycle;

import uk.gov.justice.services.core.lifecycle.catchup.CatchupListener;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupCompletedEvent;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupStartedEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.ShutteringListener;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ObjectShutteredEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ObjectUnshutteredEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.UnshutteringRequestedEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.enterprise.context.ApplicationScoped;

/**
 * Controller for sending messages as events throughout the application. These events are
 * application events and are for the control of the application itself, as opposed JsonEnvelope
 * events, which are business events for business logic.
 */
@ApplicationScoped
public class ApplicationStateController {

    private final List<CatchupListener> catchupListeners = new CopyOnWriteArrayList<>();
    private final List<ShutteringListener> shutteringListeners = new CopyOnWriteArrayList<>();

    /**
     * Adds a Listener for receiving events relating to business event catchup
     *
     * @param catchupListener The listener to be added
     */
    public void addCatchupListener(final CatchupListener catchupListener) {
        catchupListeners.add(catchupListener);
    }

    /**
     * Removes a Listener from receiving events relating to business event catchup
     *
     * @param catchupListener The listener to be removed
     */
    public void removeCatchupListener(final CatchupListener catchupListener) {
        catchupListeners.remove(catchupListener);
    }

    /**
     * Adds a Listener for receiving events relating to the shuttering of services
     *
     * @param shutteringListener The listener to be added
     */
    public void addShutteringListener(final ShutteringListener shutteringListener) {
        shutteringListeners.add(shutteringListener);
    }

    /**
     * Removes a Listener from receiving events relating to the shuttering of services
     *
     * @param shutteringListener The listener to be removed
     */
    public void removeShutteringListener(final ShutteringListener shutteringListener) {
        shutteringListeners.remove(shutteringListener);
    }

    /**
     * Called by any service that wishes to initiate catchup. For example a JMX MBean. Any service
     * that need to perform some activity before catchup (for example shuttering) should implement
     * this method and add itself as a listener to this controller.
     */
    public void fireCatchupRequested(final CatchupRequestedEvent catchupRequestedEvent) {
        catchupListeners.forEach(catchupListener -> catchupListener.catchupRequested(catchupRequestedEvent));
    }

    /**
     * Called by the catchup process to inform other parts of the system that catchup has started
     */
    public void fireCatchupStarted(final CatchupStartedEvent catchupStartedEvent) {
        catchupListeners.forEach(catchupStateListener -> catchupStateListener.catchupStarted(catchupStartedEvent));
    }

    /**
     * Called by the catchup process to inform other parts of the system that catchup has completed
     */
    public void fireCatchupCompleted(final CatchupCompletedEvent catchupCompletedEvent) {
        catchupListeners.forEach(catchupStateListener -> catchupStateListener.catchupCompleted(catchupCompletedEvent));
    }

    /**
     * Called by any service that wishes to initiate the shuttering of services. For example a JMX
     * MBean. Any service that need to perform some activity before shuttering should implement this
     * method and add itself as a listener to this controller.
     */
    public void fireShutteringRequested(final ShutteringRequestedEvent shutteringRequestedEvent) {
        shutteringListeners.forEach(shutteringListener -> shutteringListener.shutteringRequested(shutteringRequestedEvent));
    }

    /**
     * Fired by any Shutterable Object to inform listeners that its shuttering is complete
     */
    public void fireObjectShuttered(final ObjectShutteredEvent objectShutteredEvent) {
        shutteringListeners.forEach(shutteringListener -> shutteringListener.objectShuttered(objectShutteredEvent));
    }

    /**
     * Fired by the shuttering process to inform all listeners that all Objects are shuttered and
     * shuttering is complete
     */
    public void fireShutteringComplete(final ShutteringCompleteEvent shutteringCompleteEvent) {
        shutteringListeners.forEach(shutteringListener -> shutteringListener.shutteringComplete(shutteringCompleteEvent));
    }

    /**
     * Called by any service that wishes to initiate the unshuttering of services. For example a JMX
     * MBean. Any service that need to perform some activity before unshuttering should implement
     * this method and add itself as a listener to this controller.
     */
    public void fireUnshutteringRequested(final UnshutteringRequestedEvent unshutteringRequestedEvent) {
        shutteringListeners.forEach(shutteringListener -> shutteringListener.unshutteringRequested(unshutteringRequestedEvent));
    }

    /**
     * Fired by any Shutterable Object to inform listeners that its unshuttering is complete
     */
    public void fireObjectUnshuttered(final ObjectUnshutteredEvent objectUnshutteredEvent) {
        shutteringListeners.forEach(shutteringListener -> shutteringListener.objectUnshuttered(objectUnshutteredEvent));
    }

    /**
     * Fired by the shuttering process to inform all listeners that all Objects are shuttered and
     * shuttering is complete
     */
    public void fireUnshutteringComplete(final UnshutteringCompleteEvent unshutteringCompleteEvent) {
        shutteringListeners.forEach(shutteringListener -> shutteringListener.unshutteringComplete(unshutteringCompleteEvent));
    }
}
