package uk.gov.justice.services.test.utils.core.http;


import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

import uk.gov.justice.services.test.utils.core.rest.RestClient;

import javax.ws.rs.core.Response;

public class HttpResponsePoller {

    private static final int DEFAULT_DELAY_IN_MILLIS = 1000;
    private static final int RETRY_COUNT = 5;

    private final RestClient restClient = new RestClient();

    public String pollUntilNotFound(final String url, final String mediaType) {

        return pollUntilExpectedResponse(url, mediaType, DEFAULT_DELAY_IN_MILLIS, NOT_FOUND);
    }

    public String pollUntilFound(final String url, final String mediaType) {

        return pollUntilExpectedResponse(url, mediaType, DEFAULT_DELAY_IN_MILLIS, OK);
    }

    public String pollUntilExpectedResponse(final String url, final String mediaType, final int delayInMillis, final Response.Status status) {

        for (int i = 1; i <= RETRY_COUNT; i++) {
            final Response response = restClient.query(url, mediaType);
            if (response.getStatus() == status.getStatusCode()) {
                return response.readEntity(String.class);
            }

            if (i == RETRY_COUNT) {
                throw new AssertionError("Failed to get " + status.getStatusCode()  + " response from '" + url + "' after " + RETRY_COUNT + " attempts. Status code: " + response.getStatus());
            }
            sleepFor(delayInMillis);
        }

        throw new IllegalStateException("Should never get here");
    }

    public Response get(final String url, final String mediaType) {
        return restClient.query(url, mediaType);
    }

    public void sleepFor(long milliseconds) {

        try {
            sleep(milliseconds);
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
