package uk.gov.justice.services.core.dispatcher;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.handler.registry.HandlerRegistry;

import javax.inject.Inject;

import org.slf4j.Logger;

public class DispatcherFactory {

    private static final Logger LOGGER = getLogger(HandlerRegistry.class);

    private EnvelopeTypeConverter envelopeTypeConverter;
    private JsonEnvelopeConverter jsonEnvelopeConverter;

    @Inject
    public DispatcherFactory(final EnvelopeTypeConverter envelopeTypeConverter, final JsonEnvelopeConverter jsonEnvelopeConverter) {
        this.envelopeTypeConverter = envelopeTypeConverter;
        this.jsonEnvelopeConverter = jsonEnvelopeConverter;
    }

    public Dispatcher createNew() {
        return new Dispatcher(new HandlerRegistry(LOGGER), envelopeTypeConverter, jsonEnvelopeConverter);
    }
}
