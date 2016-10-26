package uk.gov.justice.services.test.utils.core.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.apache.http.HttpHost;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.junit.Test;

public class ResteasyClientBuilderFactoryTest {

    private static final String PROPERTY_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String PROPERTY_HTTP_PROXY_PORT = "http.proxyPort";
    private static final String PROXY_HOST = "proxy.moj.com";
    private static final int PROXY_PORT = 3125;

    @Test
    public void shouldSetProxyIfConfigured() throws Exception {
        givenProxyIsConfigured();

        final ResteasyClient client = ResteasyClientBuilderFactory.clientBuilder().build();
        final HttpHost actualProxySettings = ((ApacheHttpClient4Engine) client.httpEngine()).getDefaultProxy();

        assertThat(actualProxySettings.getHostName(), is(PROXY_HOST));
        assertThat(actualProxySettings.getPort(), is(PROXY_PORT));

        clearProxyConfiguration();
    }

    @Test
    public void shouldNotSetProxyIfNotConfigured() throws Exception {
        givenProxyIsNotConfigured();

        final ResteasyClient client = ResteasyClientBuilderFactory.clientBuilder().build();
        final HttpHost actualProxySettings = ((ApacheHttpClient4Engine) client.httpEngine()).getDefaultProxy();

        assertThat(actualProxySettings, is(nullValue()));
    }

    private void givenProxyIsNotConfigured() {
        assertThat(System.getProperty(PROPERTY_HTTP_PROXY_HOST), is(nullValue()));
        assertThat(System.getProperty(PROPERTY_HTTP_PROXY_PORT), is(nullValue()));
    }

    private void clearProxyConfiguration() {
        System.clearProperty(PROPERTY_HTTP_PROXY_HOST);
        System.clearProperty(PROPERTY_HTTP_PROXY_PORT);
    }

    private void givenProxyIsConfigured() {
        System.setProperty(PROPERTY_HTTP_PROXY_HOST, PROXY_HOST);
        System.setProperty(PROPERTY_HTTP_PROXY_PORT, String.valueOf(PROXY_PORT));
    }
}