package uk.gov.justice.services.core.sender;

import static uk.gov.justice.services.core.annotation.Component.EVENT_API;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.annotation.ComponentNameUtil.componentFrom;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherDelegate;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandler;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.jms.JmsSenderWrapper;
import uk.gov.justice.services.core.json.JsonSchemaValidator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Produces the correct Sender based on the injection point.
 */
@ApplicationScoped
public class SenderProducer {

    @Inject
    SenderFactory senderFactory;

    @Inject
    ComponentDestination componentDestination;

    @Inject
    DispatcherCache dispatcherCache;

    @Inject
    SystemUserUtil systemUserUtil;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    JsonSchemaValidator jsonSchemaValidator;

    @Inject
    EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;

    private Map<String, Sender> senderMap;

    public SenderProducer() {
        senderMap = new ConcurrentHashMap<>();
    }

    /**
     * Produces the correct Sender based on the injection point.
     *
     * @param injectionPoint injection point where the Sender is being injected into.
     * @return An implementation of the Sender.
     */
    @Produces
    public Sender produce(final InjectionPoint injectionPoint) {
        return getSender(componentFrom(injectionPoint), injectionPoint);
    }

    private Sender getSender(final String component, final InjectionPoint injectionPoint) {
        return new JmsSenderWrapper(primarySenderFor(injectionPoint), legacySenderFor(component));
    }

    private Optional<Sender> legacySenderFor(final String component) {
        if (isLegacySenderRequiredFor(component)) {
            return Optional.of(senderMap.computeIfAbsent(component, this::createSender));
        }

        return Optional.empty();
    }

    private boolean isLegacySenderRequiredFor(final String componentName) {
        return !componentName.equals(EVENT_PROCESSOR) && !componentName.equals(EVENT_API) && isFrameworkComponent(componentName);
    }

    private Sender createSender(final String component) {
        return senderFactory.createSender(componentDestination.getDefault(component));
    }

    private boolean isFrameworkComponent(final String component) {
        return Component.contains(component);
    }

    private Sender primarySenderFor(final InjectionPoint injectionPoint) {
        return new DispatcherDelegate(dispatcherCache.dispatcherFor(injectionPoint), systemUserUtil,
                new EnvelopeValidator(jsonSchemaValidator, envelopeValidationExceptionHandler, objectMapper));
    }
}