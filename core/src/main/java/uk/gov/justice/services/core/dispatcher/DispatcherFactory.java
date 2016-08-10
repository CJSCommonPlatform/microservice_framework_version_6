package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.event.buffer.api.EventBufferService;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DispatcherFactory {

    @Inject
    AccessControlService accessControlService;

    @Inject
    AccessControlFailureMessageGenerator accessControlFailureMessageGenerator;

    @Inject
    EventBufferService eventBufferService;

    public Dispatcher createNew(final ServiceComponentLocation location) {
        final HandlerRegistry handlerRegistry = new HandlerRegistry();

        return new Dispatcher(
                handlerRegistry,
                accessControlServiceFor(location),
                eventBufferService,
                accessControlFailureMessageGenerator
        );
    }

    private Optional<AccessControlService> accessControlServiceFor(final ServiceComponentLocation location) {
        return LOCAL.equals(location) ? Optional.of(accessControlService) : Optional.empty();
    }
}
