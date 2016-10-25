package uk.gov.justice.services.test.utils.core.rest;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public class ResteasyClientBuilderFactory {

    private static final String PROPERTY_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String PROPERTY_HTTP_PROXY_PORT = "http.proxyPort";

    public static ResteasyClientBuilder clientBuilder() {
        final ResteasyClientBuilder resteasyClientBuilder = new ResteasyClientBuilder();

        setProxyDetails(resteasyClientBuilder);

        return resteasyClientBuilder;
    }

    private static void setProxyDetails(final ResteasyClientBuilder resteasyClientBuilder) {
        final String proxyHost = System.getProperty(PROPERTY_HTTP_PROXY_HOST) != null ? System.getProperty(PROPERTY_HTTP_PROXY_HOST) : null;
        final int proxyPort = Integer.valueOf(System.getProperty(PROPERTY_HTTP_PROXY_PORT) != null ? System.getProperty(PROPERTY_HTTP_PROXY_PORT) : "-1");

        if (proxyHost != null && proxyPort != -1) {
            resteasyClientBuilder.defaultProxy(proxyHost, proxyPort);
        }
    }

}
