package uk.gov.justice.services.adapter.rest.processor;

import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.componentFrom;

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

    private RestProcessor defaultRestProcessor;

    private RestProcessor payloadOnlyRestProcessor;

    @PostConstruct
    void initialise() {
        defaultRestProcessor = new RestProcessor(envelopeBuilderFactory, envelope -> jsonObjectEnvelopeConverter.fromEnvelope(envelope).toString(), false);
        payloadOnlyRestProcessor = new RestProcessor(envelopeBuilderFactory, envelope -> envelope.payloadAsJsonObject().toString(), true);
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
        return componentFrom(injectionPoint) == QUERY_API ? payloadOnlyRestProcessor : defaultRestProcessor;
    }

}
