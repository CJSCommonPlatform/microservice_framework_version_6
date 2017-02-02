package uk.gov.justice.services.core.sender;

import static uk.gov.justice.services.core.annotation.Component.EVENT_API;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.annotation.Component.valueOf;
import static uk.gov.justice.services.core.annotation.ComponentNameUtil.componentFrom;

import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherDelegate;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandler;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.jms.JmsSenderWrapper;
import uk.gov.justice.services.core.jms.SenderFactory;
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

    private Sender getSender(final String componentName, final InjectionPoint injectionPoint) {
        final Sender primarySender = produceSender(injectionPoint);
        final Optional<Sender> legacySender = Optional.ofNullable(!componentName.equals(EVENT_PROCESSOR.name()) && !componentName.equals(EVENT_API.name()) && isFrameworkComponent(componentName) ?
                senderMap.computeIfAbsent(componentName, c -> senderFactory.createSender(componentDestination.getDefault(valueOf(c)))) : null);

        return new JmsSenderWrapper(primarySender, legacySender);
    }

    private boolean isFrameworkComponent(final String componentName) {
        try {
            valueOf(componentName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Sender produceSender(final InjectionPoint injectionPoint) {
        return new DispatcherDelegate(dispatcherCache.dispatcherFor(injectionPoint), systemUserUtil,
                new EnvelopeValidator(jsonSchemaValidator, envelopeValidationExceptionHandler, objectMapper));
    }

}
