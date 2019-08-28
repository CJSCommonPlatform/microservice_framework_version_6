package uk.gov.justice.services.jmx.bootstrap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ObjectFactoryTest {

    private final ObjectFactory objectFactory = new ObjectFactory();

    @Test
    public void shouldCreateCdiInstanceResolver() throws Exception {
        assertThat(objectFactory.cdiInstanceResolver(), is(notNullValue()));
    }

    @Test
    public void shouldCreateSystemCommandHandlerProxyFactory() throws Exception {
        assertThat(objectFactory.systemCommandHandlerProxyFactory(), is(notNullValue()));
    }

    @Test
    public void shouldCreateSystemCommandProxyResolver() throws Exception {
        assertThat(objectFactory.systemCommandProxyResolver(), is(notNullValue()));
    }

    @Test
    public void shouldCreateSystemCommandScanner() throws Exception {
        assertThat(objectFactory.systemCommandScanner(), is(notNullValue()));
    }

    @Test
    public void shouldCreateHandlerMethodValidator() throws Exception {
        assertThat(objectFactory.handlerMethodValidator(), is(notNullValue()));
    }

    @Test
    public void shouldCreateBlacklistedCommandsScanner() throws Exception {
        assertThat(objectFactory.blacklistedCommandsScanner(), is(notNullValue()));
    }

    @Test
    public void shouldCreateBlacklistedCommandsFilter() throws Exception {
        assertThat(objectFactory.blacklistedCommandsFilter(), is(notNullValue()));
    }
}
