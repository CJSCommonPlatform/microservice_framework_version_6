package uk.gov.justice.services.test.utils.rest;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Helper class to send Post Commands and Get Queries.
 */
public class RestClient {

    /**
     * POSTs a command to the specified URL.
     *
     * @param url            - the URL to post the command to.
     * @param contentType    - the content type of the command.
     * @param requestPayload - the payload of the command.
     * @return the Response from the command being issued.
     */
    public Response postCommand(final String url, final String contentType, final String requestPayload) {
        final Entity<String> entity = entity(requestPayload, MediaType.valueOf(contentType));

        return ClientBuilder.newClient()
                .target(url)
                .request()
                .post(entity);
    }

    /**
     * POSTs a command with headers to the specified URL.
     *
     * @param url            - the URL to post the command to.
     * @param contentType    - the content type of the command.
     * @param requestPayload - the payload of the command.
     * @param headers        - headers to be sent in the request.
     * @return the Response from the command being issued.
     */
    public Response postCommand(final String url, final String contentType, final String requestPayload, final MultivaluedMap<String, Object> headers) {
        final Entity<String> entity = entity(requestPayload, MediaType.valueOf(contentType));

        return ClientBuilder.newClient()
                .target(url)
                .request()
                .headers(headers)
                .post(entity);
    }

    /**
     * Sends a query (Get) to the specified URL.
     *
     * @param url          - the URL of the query.
     * @param contentTypes - the content type of the query.
     * @return the Response from the query being issued.
     */
    public Response query(final String url, final String contentTypes) {
        return ClientBuilder.newClient()
                .target(url)
                .request(MediaType.valueOf(contentTypes))
                .get();
    }

    /**
     * Sends a query (Get) with headers to the specified URL.
     *
     * @param url          - the URL of the query.
     * @param contentTypes - the content type of the query.
     * @param headers      - headers to be sent in the request.
     * @return the Response from the query being issued.
     */
    public Response query(final String url, final String contentTypes, final MultivaluedMap<String, Object> headers) {
        return ClientBuilder.newClient()
                .target(url)
                .request()
                .headers(headers)
                .header(ACCEPT, contentTypes)
                .get();
    }
}
