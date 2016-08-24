package uk.gov.justice.services.test.utils.core.http;


import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.util.function.Predicate;

import javax.ws.rs.core.Response;

public class HttpResponsePoller {

    private static final int DEFAULT_DELAY_IN_MILLIS = 1000;
    private static final int RETRY_COUNT = 5;

    private final RestClient restClient;

    public HttpResponsePoller() {
        this.restClient = new RestClient();
    }

    public HttpResponsePoller(final RestClient restClient) {
        this.restClient = restClient;
    }

    public String pollUntilNotFound(final String url, final String mediaType) {
        return pollUntilExpectedResponse(url, mediaType, DEFAULT_DELAY_IN_MILLIS, NOT_FOUND);
    }

    public String pollUntilFound(final String url, final String mediaType) {
        return pollUntilExpectedResponse(url, mediaType, DEFAULT_DELAY_IN_MILLIS, OK);
    }

    public String pollUntilFoundWithCondition(final String url, final String mediaType, final int delayInMillis, final Predicate<String> condition) {
        final Predicate<Response> doesResponseValueMeetCondition = response -> {
            final String value = response.readEntity(String.class);
            return value != null && condition.test(value);
        };

        return pollUntilExpectedResponse(url, mediaType, delayInMillis, doesResponseValueMeetCondition).readEntity(String.class);
    }

    public String pollUntilExpectedResponse(final String url, final String mediaType, final int delayInMillis, final Response.Status status) {
        final Response result = pollUntilExpectedResponse(url, mediaType, delayInMillis, response -> response.getStatus() == status.getStatusCode());
        return result.readEntity(String.class);
    }

    private Response pollUntilExpectedResponse(final String url, final String mediaType, final int delayInMillis, final Predicate<Response> condition) {
        for (int i = 1; i <= RETRY_COUNT; i++) {
            final Response response = restClient.query(url, mediaType);

            if (condition.test(response)) {
                return response;
            }

            if (i == RETRY_COUNT) {
                throw new AssertionError("Failed to get " + response.getStatus() + " response from '" + url + "' after " + RETRY_COUNT + " attempts. Status code: " + response.getStatus());
            }

            sleepFor(delayInMillis);
        }

        throw new IllegalStateException("Should never get here");
    }

    public Response get(final String url, final String mediaType) {
        return restClient.query(url, mediaType);
    }

    private void sleepFor(long milliseconds) {

        try {
            sleep(milliseconds);
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
