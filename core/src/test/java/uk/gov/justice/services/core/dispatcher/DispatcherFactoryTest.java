package uk.gov.justice.services.core.dispatcher;

import static org.apache.commons.lang3.reflect.FieldUtils.readField;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.core.handler.registry.HandlerRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherFactoryTest {

    private static final String HANDLER_REGISTRY_FIELD_NAME = "handlerRegistry";
    private static final String LOGGER_FIELD_NAME = "logger";

    @InjectMocks
    private DispatcherFactory dispatcherFactory;

    @Test
    public void shouldCreateNewDispatcher() throws Exception {
        assertThat(dispatcherFactory.createNew(), instanceOf(Dispatcher.class));
    }

    @Test
    public void shouldCreateANewHandlerRegistryForEachDispatcherInstance() throws Exception {

        final Dispatcher dispatcher1 = dispatcherFactory.createNew();
        final Dispatcher dispatcher2 = dispatcherFactory.createNew();

        final HandlerRegistry handlerRegistry1 = getHandlerRegistryFrom(dispatcher1);
        final HandlerRegistry handlerRegistry2 = getHandlerRegistryFrom(dispatcher2);

        assertThat(handlerRegistry1, is(not(sameInstance(handlerRegistry2))));
        assertThat(getLoggerFieldFrom(handlerRegistry1), is(notNullValue()));
        assertThat(getLoggerFieldFrom(handlerRegistry2), is(notNullValue()));
    }

    private HandlerRegistry getHandlerRegistryFrom(final Dispatcher dispatcher) throws IllegalAccessException {
        return (HandlerRegistry) readField(dispatcher, HANDLER_REGISTRY_FIELD_NAME, true);
    }

    private Logger getLoggerFieldFrom(final HandlerRegistry handlerRegistry) throws IllegalAccessException {
        return (Logger) readField(handlerRegistry, LOGGER_FIELD_NAME, true);
    }
}