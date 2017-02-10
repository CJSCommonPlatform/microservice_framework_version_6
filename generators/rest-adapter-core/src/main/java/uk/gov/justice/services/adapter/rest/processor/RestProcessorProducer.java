package uk.gov.justice.services.adapter.rest.processor;

import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.core.annotation.ComponentNameUtil.componentFrom;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * Produces the correct implementation of RestProcessor depending on the Adapter's Component type.
 */
@ApplicationScoped
public class RestProcessorProducer {

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    RestEnvelopeBuilderFactory envelopeBuilderFactory;

    private RestProcessor envelopeResponseRestProcessor;

    private RestProcessor payloadResponseRestProcessor;

    @PostConstruct
    void initialise() {
        envelopeResponseRestProcessor = new EnvelopeResponseRestProcessor(envelopeBuilderFactory, jsonObjectEnvelopeConverter);
        payloadResponseRestProcessor = new PayloadResponseRestProcessor(envelopeBuilderFactory);
    }

    /**
     * Produces the correct implementation of a {@link RestProcessor} depending on the Adapter
     * annotation at the injection point.
     *
     * @param injectionPoint class where the {@link RestProcessor} is being injected
     * @return the correct RestProcessor instance
     * @throws IllegalStateException if the injection point does not have an Adapter annotation
     */
    @Produces
    public RestProcessor produceRestProcessor(final InjectionPoint injectionPoint) {
        final String componentName = componentFrom(injectionPoint);
        return QUERY_CONTROLLER.equals(componentName) || QUERY_VIEW.equals(componentName) ? envelopeResponseRestProcessor : payloadResponseRestProcessor;
    }

}
