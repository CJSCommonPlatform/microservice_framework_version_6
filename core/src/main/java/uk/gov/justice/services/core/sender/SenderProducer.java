package uk.gov.justice.services.core.sender;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherDelegate;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeInspector;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandler;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;

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
    JsonSchemaValidator jsonSchemaValidator;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;

    @Inject
    NameToMediaTypeConverter nameToMediaTypeConverter;
    
    @Inject
    MediaTypeProvider mediaTypeProvider;
    
    @Inject
    EnvelopeInspector envelopeInspector;

    @Inject
    DispatcherCache dispatcherCache;

    @Inject
    SystemUserUtil systemUserUtil;


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
        final EnvelopeValidator envelopeValidator = new EnvelopeValidator(
                jsonSchemaValidator,
                objectMapper,
                envelopeValidationExceptionHandler
        );

        final RequestResponseEnvelopeValidator requestResponseEnvelopeValidator = new RequestResponseEnvelopeValidator(
                envelopeValidator,
                nameToMediaTypeConverter,
                mediaTypeProvider,
                envelopeInspector);

        return new DispatcherDelegate(
                dispatcherCache.dispatcherFor(injectionPoint),
                systemUserUtil,
                requestResponseEnvelopeValidator);
    }
}
