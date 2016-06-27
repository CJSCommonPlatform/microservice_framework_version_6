package uk.gov.justice.services.core.dispatcher;

import static org.apache.commons.lang3.reflect.FieldUtils.readField;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;

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
    public void shouldCreateANewDispatcherUsingTheInjectedDependencies() throws Exception {

        final Dispatcher dispatcher = dispatcherFactory.createNew();
        final AccessControlService accessControlServiceFromClass = getAccessControlServiceFrom(dispatcher);
        final AccessControlFailureMessageGenerator accessControlFailureMessageGeneratorFromClass = getFailureMessageGeneratorFrom(dispatcher);

        assertThat(accessControlServiceFromClass, is(sameInstance(accessControlService)));
        assertThat(accessControlFailureMessageGeneratorFromClass, is(sameInstance(accessControlFailureMessageGenerator)));
    }

    @Test
    public void shouldCreateANewHandlerRegistryForEachDispatcherInstance() throws Exception {

        final Dispatcher dispatcher_1 = dispatcherFactory.createNew();
        final Dispatcher dispatcher_2 = dispatcherFactory.createNew();

        final HandlerRegistry handlerRegistry_1 = getHandlerRegistryFrom(dispatcher_1);
        final HandlerRegistry handlerRegistry_2 = getHandlerRegistryFrom(dispatcher_2);

        assertThat(handlerRegistry_1, is(not(sameInstance(handlerRegistry_2))));
    }

    private HandlerRegistry getHandlerRegistryFrom(final Dispatcher dispatcher_1) throws IllegalAccessException {
        return (HandlerRegistry) readField(dispatcher_1, HANDLER_REGISTRY_FIELD_NAME, true);
    }

    private AccessControlFailureMessageGenerator getFailureMessageGeneratorFrom(final Dispatcher dispatcher) throws IllegalAccessException {
        return (AccessControlFailureMessageGenerator) readField(dispatcher, FAILURE_MESSAGE_GENERATOR_FIELD_NAME, true);
    }

    private AccessControlService getAccessControlServiceFrom(final Dispatcher dispatcher) throws IllegalAccessException {
        return (AccessControlService) readField(dispatcher, ACCESS_CONTROL_SERVICE_FIELD_NAME, true);
    }
}
