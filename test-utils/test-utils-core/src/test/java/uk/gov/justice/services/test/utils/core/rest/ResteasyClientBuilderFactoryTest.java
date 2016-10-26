package uk.gov.justice.services.test.utils.core.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.apache.http.HttpHost;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResteasyClientBuilderFactoryTest {

    private static final String PROPERTY_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String PROPERTY_HTTP_PROXY_PORT = "http.proxyPort";
    private static final String PROXY_HOST = "proxy.moj.com";
    private static final int PROXY_PORT = 3125;
    private String proxyHostValue;
    private String proxyPortValue;

    @Before
    public void setUp() {
        proxyHostValue = System.getProperty(PROPERTY_HTTP_PROXY_HOST);
        proxyPortValue = System.getProperty(PROPERTY_HTTP_PROXY_PORT);
    }

    @After
    public void tearDown() {
        System.clearProperty(PROPERTY_HTTP_PROXY_HOST);
        System.clearProperty(PROPERTY_HTTP_PROXY_PORT);

        if (proxyHostValue != null) {
            System.setProperty(PROPERTY_HTTP_PROXY_HOST, proxyHostValue);
        }
        if (proxyPortValue != null) {
            System.setProperty(PROPERTY_HTTP_PROXY_PORT, proxyPortValue);
        }
    }

    @Test
    public void shouldSetProxyIfConfigured() throws Exception {
        System.setProperty(PROPERTY_HTTP_PROXY_HOST, PROXY_HOST);
        System.setProperty(PROPERTY_HTTP_PROXY_PORT, String.valueOf(PROXY_PORT));

        final ResteasyClient client = ResteasyClientBuilderFactory.clientBuilder().build();
        final HttpHost actualProxySettings = ((ApacheHttpClient4Engine) client.httpEngine()).getDefaultProxy();

        assertThat(actualProxySettings.getHostName(), is(PROXY_HOST));
        assertThat(actualProxySettings.getPort(), is(PROXY_PORT));
    }

    @Test
    public void shouldNotSetProxyIfNotConfigured() throws Exception {
        System.clearProperty(PROPERTY_HTTP_PROXY_HOST);
        System.clearProperty(PROPERTY_HTTP_PROXY_PORT);

        final ResteasyClient client = ResteasyClientBuilderFactory.clientBuilder().build();
        final HttpHost actualProxySettings = ((ApacheHttpClient4Engine) client.httpEngine()).getDefaultProxy();

        assertThat(actualProxySettings, is(nullValue()));
    }

}