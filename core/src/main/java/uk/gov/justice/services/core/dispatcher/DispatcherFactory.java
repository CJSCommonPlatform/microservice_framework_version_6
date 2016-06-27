package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DispatcherFactory {

    @Inject
    private AccessControlService accessControlService;

    @Inject
    private AccessControlFailureMessageGenerator accessControlFailureMessageGenerator;

     public Dispatcher createNew() {
        final HandlerRegistry handlerRegistry = new HandlerRegistry();

        return new Dispatcher(
                handlerRegistry,
                accessControlService,
                accessControlFailureMessageGenerator
        );
    }
}
