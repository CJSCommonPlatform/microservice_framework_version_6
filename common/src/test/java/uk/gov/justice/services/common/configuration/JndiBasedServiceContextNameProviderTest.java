package uk.gov.justice.services.common.configuration;

import static org.junit.Assert.*;

import org.junit.Test;

public class JndiBasedServiceContextNameProviderTest {

    private static final String APP_NAME = "App";

    private JndiBasedServiceContextNameProvider jndiBasedServiceContextNameProvider;

    @Test
    public void shouldReturnContextName() {
        jndiBasedServiceContextNameProvider = new JndiBasedServiceContextNameProvider();
        jndiBasedServiceContextNameProvider.appName = APP_NAME;

        final String serviceContextName = jndiBasedServiceContextNameProvider.getServiceContextName();

        assertEquals(serviceContextName, APP_NAME);
    }

    @Test
    public void shouldReturnContextNameFromConstructor() {
        jndiBasedServiceContextNameProvider = new JndiBasedServiceContextNameProvider(APP_NAME);

        final String serviceContextName = jndiBasedServiceContextNameProvider.getServiceContextName();

        assertEquals(serviceContextName, APP_NAME);
    }
}