package uk.gov.justice.services.core.dispatcher;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.handler.registry.HandlerRegistry;

import javax.inject.Inject;

import org.slf4j.Logger;

public class DispatcherFactory {

    private static final Logger LOGGER = getLogger(HandlerRegistry.class);

    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;
    private JsonEnvelopeRepacker jsonEnvelopeRepacker;

    @Inject
    public DispatcherFactory(final EnvelopePayloadTypeConverter envelopePayloadTypeConverter, final JsonEnvelopeRepacker jsonEnvelopeRepacker) {
        this.envelopePayloadTypeConverter = envelopePayloadTypeConverter;
        this.jsonEnvelopeRepacker = jsonEnvelopeRepacker;
    }

    public Dispatcher createNew() {
        return new Dispatcher(new HandlerRegistry(LOGGER), envelopePayloadTypeConverter, jsonEnvelopeRepacker);
    }
}
