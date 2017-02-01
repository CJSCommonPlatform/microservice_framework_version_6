package uk.gov.justice.services.test.utils.core.rest;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static uk.gov.justice.services.test.utils.core.rest.ResteasyClientBuilderFactory.clientBuilder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to send Post Commands and Get Queries.
 */
public class RestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);

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

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Making POST request to '{}' with Content Type '{}'", url, contentType);
            LOGGER.info("Request payload: '{}'", requestPayload);
        }

        final Response response = clientBuilder().build()
                .target(url)
                .request()
                .post(entity);

        if (LOGGER.isInfoEnabled()) {
            final Response.StatusType statusType = response.getStatusInfo();
            LOGGER.info("Received response status '{}' '{}'", statusType.getStatusCode(), statusType.getReasonPhrase());
        }

        return response;
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


        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Making POST request to '{}' with Content Type '{}'", url, contentType);
            LOGGER.info("Request payload: '{}'", requestPayload);
            LOGGER.info("Headers: {}", headers);
        }


        final Response response = clientBuilder().build()
                .target(url)
                .request()
                .headers(headers)
                .post(entity);

        if (LOGGER.isInfoEnabled()) {
            final Response.StatusType statusType = response.getStatusInfo();
            LOGGER.info("Received response status '{}' '{}'", statusType.getStatusCode(), statusType.getReasonPhrase());
        }

        return response;
    }

    /**
     * Sends a query (Get) to the specified URL.
     *
     * @param url          - the URL of the query.
     * @param contentTypes - the content type of the query.
     * @return the Response from the query being issued.
     */
    public Response query(final String url, final String contentTypes) {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Making GET request to '{}' with Content Type '{}'", url, contentTypes);
        }

        final Response response = clientBuilder().build()
                .target(url)
                .request(MediaType.valueOf(contentTypes))
                .get();

        if (LOGGER.isInfoEnabled()) {
            final Response.StatusType statusType = response.getStatusInfo();
            LOGGER.info("Received response status '{}' '{}'", statusType.getStatusCode(), statusType.getReasonPhrase());
        }

        return response;
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

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Making GET request to '{}' with Content Type '{}'", url, contentTypes);
            LOGGER.info("Headers: {}", headers);
        }

        final Response response = clientBuilder().build()
                .target(url)
                .request()
                .headers(headers)
                .header(ACCEPT, contentTypes)
                .get();

        if (LOGGER.isInfoEnabled()) {
            final Response.StatusType statusType = response.getStatusInfo();
            LOGGER.info("Received response status '{}' '{}'", statusType.getStatusCode(), statusType.getReasonPhrase());
        }
        
        return response;
    }
}
