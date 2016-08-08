package uk.gov.justice.services.test.utils.http;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static javax.ws.rs.core.Response.Status.OK;

import uk.gov.justice.services.test.utils.rest.RestClient;

import javax.ws.rs.core.Response;

public class HttpResponsePoller {

    private static final int DEFAULT_DELAY_IN_MILLIS = 1000;
    private static final int RETRY_COUNT = 5;

    private final RestClient restClient = new RestClient();

    public String pollForResponse(final String url, final String mediaType) {

        return pollForResponse(url, mediaType, DEFAULT_DELAY_IN_MILLIS);
    }

    public String pollForResponse(final String url, final String mediaType, int delayInMillis) {

        for (int i = 1; i <= RETRY_COUNT; i++) {
            final Response response = restClient.query(url, mediaType);
            if (response.getStatus() == OK.getStatusCode()) {
                return response.readEntity(String.class);
            }

            if (i == RETRY_COUNT) {
                throw new AssertionError("Failed to get 200 response from '" + url + "' after " + RETRY_COUNT + " attempts. Status code: " + response.getStatus());
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
