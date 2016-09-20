package uk.gov.justice.services.test.utils.core.http;


import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class HttpResponsePoller {

    private static final int DEFAULT_DELAY_IN_MILLIS = 1000;
    private static final int RETRY_COUNT = 5;

    private final RestClient restClient;

    private MultivaluedMap<String, Object> headers;

    public HttpResponsePoller() {
        this.restClient = new RestClient();
    }

    public HttpResponsePoller(final RestClient restClient) {
        this.restClient = restClient;
    }

    public HttpResponsePoller withHeaders(final MultivaluedMap<String, Object> headers) {
        this.headers = headers;
        return this;
    }

    public String pollUntilNotFound(final String url, final String mediaType) {
        return pollUntilExpectedResponse(url, mediaType, DEFAULT_DELAY_IN_MILLIS, NOT_FOUND);
    }

    public String pollUntilFound(final String url, final String mediaType) {
        return pollUntilExpectedResponse(url, mediaType, DEFAULT_DELAY_IN_MILLIS, OK);
    }

    public String pollUntilJsonObjectFoundWithValues(final String url, final String mediaType, final Map<String, String> values) {
        return pollUntilFoundWithCondition(url, mediaType, compareJsonObjectWith(values));
    }

    public String pollUntilFoundWithCondition(final String url, final String mediaType, final Predicate<String> condition) {
        return pollUntilFoundWithCondition(url, mediaType, condition, DEFAULT_DELAY_IN_MILLIS);
    }

    public String pollUntilFoundWithCondition(final String url, final String mediaType, final Predicate<String> condition, final int delayInMillis) {
        return pollUntilExpectedResponse(
                url,
                mediaType,
                delayInMillis,
                response -> response.getStatus() == OK.getStatusCode(),
                condition
        );
    }

    public String pollUntilExpectedResponse(final String url, final String mediaType, final int delayInMillis, final Response.Status status) {
        return pollUntilExpectedResponse(
                url,
                mediaType,
                delayInMillis,
                response -> response.getStatus() == status.getStatusCode(),
                value -> true
        );
    }

    public Response get(final String url, final String mediaType) {
        if (null != headers) {
            return restClient.query(url, mediaType, headers);
        }
        return restClient.query(url, mediaType);
    }

    public void sleepFor(final long milliseconds) {
        try {
            sleep(milliseconds);
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }

    private Predicate<String> compareJsonObjectWith(final Map<String, String> values) {
        return entity -> {
            if (entity != null) {
                final JsonReader jsonReader = Json.createReader(new StringReader(entity));
                final JsonObject jsonObject = jsonReader.readObject();
                jsonReader.close();

                final boolean anyMatchFalse = values.entrySet().stream().map(entry -> {
                    final Optional<String> value = JsonObjects.getString(jsonObject, entry.getKey());
                    return value.isPresent() && value.get().equals(entry.getValue());
                }).anyMatch(e -> !e);

                return !anyMatchFalse;
            }

            return false;
        };
    }

    private String pollUntilExpectedResponse(final String url,
                                             final String mediaType,
                                             final int delayInMillis,
                                             final Predicate<Response> responseCondition,
                                             final Predicate<String> entityCondition) {
        for (int i = 1; i <= RETRY_COUNT; i++) {
            final Response response = get(url, mediaType);
            final String result = response.readEntity(String.class);

            if (responseCondition.test(response) && entityCondition.test(result)) {
                return result;
            }

            if (i == RETRY_COUNT) {
                if (!responseCondition.test(response)) {
                    final int status = response.getStatus();
                    throw new AssertionError(format("Failed to match response conditions from %s, after %d attempts, with status code: %s", url, RETRY_COUNT, status));
                } else {
                    throw new AssertionError(format("Failed to match result conditions from %s, after %d attempts, with result: %s", url, RETRY_COUNT, result));
                }
            }

            sleepFor(delayInMillis);
        }

        throw new IllegalStateException("Should never get here");
    }
}
