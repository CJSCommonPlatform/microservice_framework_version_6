package uk.gov.justice.services.core.dispatcher;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.handler.registry.HandlerRegistry;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;

@ApplicationScoped
public class DispatcherFactory {

    private static final Logger LOGGER = getLogger(HandlerRegistry.class);

    public Dispatcher createNew() {
        return new DefaultDispatcher(new HandlerRegistry(LOGGER));
    }
}
