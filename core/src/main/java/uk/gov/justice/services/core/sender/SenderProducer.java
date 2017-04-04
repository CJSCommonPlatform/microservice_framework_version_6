package uk.gov.justice.services.core.sender;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherDelegate;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandler;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.json.JsonSchemaValidator;

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
    DispatcherCache dispatcherCache;

    @Inject
    SystemUserUtil systemUserUtil;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    JsonSchemaValidator jsonSchemaValidator;

    @Inject
    EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;

    /**
     * Produces the correct implementation of a requester depending on the {@link ServiceComponent}
     * annotation at the injection point.
     *
     * @param injectionPoint class where the {@link Sender} is being injected
     * @return the correct requester instance
     * @throws IllegalStateException if the injection point does not have a {@link ServiceComponent}
     *                               annotation
     */
    @Produces
    public Sender produceSender(final InjectionPoint injectionPoint) {
        return new DispatcherDelegate(dispatcherCache.dispatcherFor(injectionPoint), systemUserUtil,
                new EnvelopeValidator(jsonSchemaValidator, envelopeValidationExceptionHandler, objectMapper));
    }
}
