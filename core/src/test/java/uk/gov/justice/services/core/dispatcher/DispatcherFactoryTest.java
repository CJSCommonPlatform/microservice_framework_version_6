package uk.gov.justice.services.core.dispatcher;

import static org.apache.commons.lang3.reflect.FieldUtils.readField;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.REMOTE;

import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherFactoryTest {

    private static final String ACCESS_CONTROL_SERVICE_FIELD_NAME = "accessControlService";
    private static final String FAILURE_MESSAGE_GENERATOR_FIELD_NAME = "accessControlFailureMessageGenerator";
    private static final String HANDLER_REGISTRY_FIELD_NAME = "handlerRegistry";

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private AccessControlFailureMessageGenerator accessControlFailureMessageGenerator;

    @InjectMocks
    private DispatcherFactory dispatcherFactory;

    @Test
    public void shouldCreateDispatcherUsingTheInjectedDependencies() throws Exception {

        final Dispatcher dispatcher = dispatcherFactory.createNew(LOCAL);
        final Optional<AccessControlService> accessControlServiceFromClass = getAccessControlServiceFrom(dispatcher);
        final AccessControlFailureMessageGenerator accessControlFailureMessageGeneratorFromClass = getFailureMessageGeneratorFrom(dispatcher);

        assertThat(accessControlServiceFromClass.get(), is(sameInstance(accessControlService)));
        assertThat(accessControlFailureMessageGeneratorFromClass, is(sameInstance(accessControlFailureMessageGenerator)));
    }

    @Test
    public void shouldCreateDispatcherWithNoAccessControlForRemoteLocation() throws Exception {

        final Dispatcher dispatcher = dispatcherFactory.createNew(REMOTE);
        final Optional<AccessControlService> accessControlServiceFromClass = getAccessControlServiceFrom(dispatcher);

        assertThat(accessControlServiceFromClass.isPresent(), is(false));
    }

    @Test
    public void shouldCreateANewHandlerRegistryForEachDispatcherInstance() throws Exception {

        final Dispatcher dispatcher1 = dispatcherFactory.createNew(LOCAL);
        final Dispatcher dispatcher2 = dispatcherFactory.createNew(LOCAL);

        final HandlerRegistry handlerRegistry1 = getHandlerRegistryFrom(dispatcher1);
        final HandlerRegistry handlerRegistry2 = getHandlerRegistryFrom(dispatcher2);

        assertThat(handlerRegistry1, is(not(sameInstance(handlerRegistry2))));
    }

    private HandlerRegistry getHandlerRegistryFrom(final Dispatcher dispatcher_1) throws IllegalAccessException {
        return (HandlerRegistry) readField(dispatcher_1, HANDLER_REGISTRY_FIELD_NAME, true);
    }

    private AccessControlFailureMessageGenerator getFailureMessageGeneratorFrom(final Dispatcher dispatcher) throws IllegalAccessException {
        return (AccessControlFailureMessageGenerator) readField(dispatcher, FAILURE_MESSAGE_GENERATOR_FIELD_NAME, true);
    }

    private Optional<AccessControlService> getAccessControlServiceFrom(final Dispatcher dispatcher) throws IllegalAccessException {
        return (Optional<AccessControlService>) readField(dispatcher, ACCESS_CONTROL_SERVICE_FIELD_NAME, true);
    }
}
