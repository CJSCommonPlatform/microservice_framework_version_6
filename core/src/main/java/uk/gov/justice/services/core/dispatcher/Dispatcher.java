package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.core.handler.HandlerMethod.ASYNCHRONOUS;
import static uk.gov.justice.services.core.handler.HandlerMethod.SYNCHRONOUS;

import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.accesscontrol.AccessControlViolation;
import uk.gov.justice.services.core.accesscontrol.AccessControlViolationException;
import uk.gov.justice.services.core.handler.HandlerMethod;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

/**
 * Dispatches messages synchronously or asynchronously to their corresponding handlers, which could
 * be a command handler, command controller, event processor, etc.
 *
 * This class handles both synchronous and asynchronous dispatching. Note that it does not implement
 * the {{@link SynchronousDispatcher} or {@link AsynchronousDispatcher} interfaces. This is because
 * the <code>dispatch</code> method names would clash. Instead, we expose the dispatcher as a
 * functional interface via the {@link ServiceComponentObserver}.
 */
public class Dispatcher {

    private final HandlerRegistry handlerRegistry;
    private final AccessControlService accessControlService;
    private final AccessControlFailureMessageGenerator accessControlFailureMessageGenerator;


    public Dispatcher(final HandlerRegistry handlerRegistry,
                      final AccessControlService accessControlService,
                      final AccessControlFailureMessageGenerator accessControlFailureMessageGenerator) {
        this.handlerRegistry = handlerRegistry;
        this.accessControlService = accessControlService;
        this.accessControlFailureMessageGenerator = accessControlFailureMessageGenerator;
    }

    /**
     * Asynchronously dispatch message to its corresponding handler, which could be a command
     * handler, command controller, event processor, etc.
     *
     * The underlying {@link HandlerMethod} will have returned a null {@link Void}, which we throw
     * away at this point to provide void method that can be exposed via the {@link
     * AsynchronousDispatcher} interface.
     *
     * @param envelope the envelope to dispatch to a handler
     */
    public void asynchronousDispatch(final JsonEnvelope envelope) {
        doDispatch(envelope, ASYNCHRONOUS);
    }

    /**
     * Synchronously dispatch message to its corresponding handler, which could be a command
     * handler, command controller, event processor, etc.
     *
     * @param envelope the envelope to dispatch to a handler
     * @return the envelope returned by the handler method
     */
    public JsonEnvelope synchronousDispatch(final JsonEnvelope envelope) {
        return doDispatch(envelope, SYNCHRONOUS);
    }

    /**
     * Registers the handler instance.
     *
     * This is only called by the {@link ServiceComponentObserver} to populate the handler
     * registry.
     *
     * @param handler handler instance to be registered.
     */
    void register(final Object handler) {
        handlerRegistry.register(handler);
    }

    private JsonEnvelope doDispatch(final JsonEnvelope envelope, final boolean isSynchronous) {
        checkAccessControl(envelope);
        return (JsonEnvelope) getMethod(envelope, isSynchronous).execute(envelope);
    }

    /**
     * Get the handler method for handling this envelope or throw an exception.
     *
     * @param envelope the envelope to be handled
     * @return the handler method
     */
    private HandlerMethod getMethod(final JsonEnvelope envelope, final boolean isSynchronous) {
        final String name = envelope.metadata().name();
        return handlerRegistry.get(name, isSynchronous);
    }

    private void checkAccessControl(final JsonEnvelope jsonEnvelope) {

        final Optional<AccessControlViolation> accessControlViolation =
                accessControlService.checkAccessControl(jsonEnvelope);

        if (accessControlViolation.isPresent()) {
            final String errorMessage = accessControlFailureMessageGenerator.errorMessageFrom(
                    jsonEnvelope,
                    accessControlViolation.get());

            throw new AccessControlViolationException(errorMessage);
        }
    }
}
