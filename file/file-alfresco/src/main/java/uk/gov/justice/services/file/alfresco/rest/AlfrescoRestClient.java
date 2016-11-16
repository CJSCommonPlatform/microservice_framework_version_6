package uk.gov.justice.services.file.alfresco.rest;

import static java.lang.String.format;

import uk.gov.justice.services.common.configuration.GlobalValue;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

@ApplicationScoped
public class AlfrescoRestClient {

    @Inject
    @GlobalValue(key = "alfrescoBaseUri")
    public String alfrescoBaseUri;

    @Inject
    @GlobalValue(key = "alfresco.proxy.type", defaultValue = "none")
    public String proxyType;

    @Inject
    @GlobalValue(key = "alfresco.proxy.hostname", defaultValue = "none")
    public String proxyHostname;

    @Inject
    @GlobalValue(key = "alfresco.proxy.port", defaultValue = "0")
    public String proxyPort;

    /**
     * Sends a message via post.
     *
     * @param uri       - the URI to post the message to.
     * @param mediaType - the mediaType of the message.
     * @param headers   - any Http headers required.
     * @param entity    - the entity to post.
     * @return the response from the Http request.
     */
    public Response post(final String uri, final MediaType mediaType, final MultivaluedHashMap<String, Object> headers, final Entity entity) {
        return getClient()
                .target(format("%s%s", alfrescoBaseUri, uri))
                .request(mediaType)
                .headers(headers)
                .post(entity);
    }

    /**
     * Request a resource via a get.
     *
     * @param uri       - the URI to request the resource from.
     * @param mediaType - the mediaType of the resource.
     * @param headers   - any Http headers required for the request.
     * @return the response from the Http request.
     */
    public Response get(final String uri, final MediaType mediaType, final MultivaluedHashMap<String, Object> headers) {
        return getClient()
                .target(format("%s%s", alfrescoBaseUri, uri))
                .request(mediaType)
                .headers(headers)
                .get();
    }

    private Client getClient() {
        Client client;

        if ("none".equals(proxyType)) {
            client = ClientBuilder.newClient();
        } else {
            client = new ResteasyClientBuilder()
                    .defaultProxy(proxyHostname, Integer.parseInt(proxyPort), proxyType)
                    .build();
        }
        return client;
    }

}
