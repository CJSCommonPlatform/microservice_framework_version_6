package uk.gov.justice.services.core.dispatcher;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.handler.registry.HandlerRegistry;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

public class DispatcherFactory {

    private static final Logger LOGGER = getLogger(HandlerRegistry.class);

    private ObjectMapper objectMapper;

    @Inject
    public DispatcherFactory(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Dispatcher createNew() {
        return new Dispatcher(new HandlerRegistry(LOGGER), objectMapper);
    }
}
