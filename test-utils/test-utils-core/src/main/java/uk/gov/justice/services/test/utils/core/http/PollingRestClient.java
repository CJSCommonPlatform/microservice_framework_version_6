package uk.gov.justice.services.test.utils.core.http;

import static java.lang.String.format;

import uk.gov.justice.services.test.utils.core.helper.Sleeper;

import java.util.Optional;

import javax.ws.rs.core.Response;

import com.google.common.annotations.VisibleForTesting;

public class PollingRestClient {

    private final ValidatingRestClient validatingRestClient;
    private final Sleeper sleeper;

    /**
     * Constructs a fully functioning PollingRestClient
     */
    public PollingRestClient() {
        this(new ValidatingRestClient(), new Sleeper());
    }

    public PollingRestClient(final ValidatingRestClient validatingRestClient, Sleeper sleeper) {
        this.validatingRestClient = validatingRestClient;
        this.sleeper = sleeper;
    }

    /**
     * Polls a rest endpoint a specified number of times (default 5), once every second (default)
     * until a successful result is returned. Will throw a Junit AssertError (failing your tests) if
     * the desired result is not returned after <i>retryCount</i> number of times, or if one of the
     * condition Predicates fails.
     *
     * To Use:
     *
     * <pre><blockquote>
     *
     *      final String url = "http://localhost:8080/my-context/my/rest/endpoint";
     *      final String mediaType = "application/vnd.notification.query.events+json";
     *
     *      final PollingRequestParams pollingRequestParams = new PollingRequestParamsBuilder(url, mediaType)
     *              .withHeader("header-name", "header-value") // override any defaults here to taste
     *              .build();
     *
     *      final String json = new PollingRestClient().pollUntilExpectedResponse(pollingRequestParams);
     *
     * </blockquote></pre>
     *
     * The call is configured using <code>PollingRequestParams</code>. This object is most easily
     * created using a <code>PollingRequestParamsBuilder</code> which takes a url and media type and
     * will supply all other required parameters with defaults. Overriding these defaults is done in
     * the usual builder pattern way
     *
     * Response and Result Conditions:
     *
     * To allow waiting until a particular value is updated, then there are two Predicates that can
     * be added to the builder. One which validates the Response Object: the Response condition, and
     * one which validates the response body as a String: the Result condition.
     *
     * These are best added using the PollingRequestParamsBuilder:
     *
     * <pre><blockquote>
     *
     *     final PollingRequestParams pollingRequestParams =
     *          new PollingRequestParamsBuilder(url, mediaType)
     *              .withResultCondition(json -> json.equals("my-json"))
     *              .withResponseCondition(response -> response.getStatus() == 200)
     *          .build();
     *
     * </blockquote></pre>
     *
     *
     * @param pollingRequestParams all parameters for polling the end point. Best created using the
     *                             @See PollingRequestParamsBuilder
     * @return the response body as a String
     *
     */
    public String pollUntilExpectedResponse(final PollingRequestParams pollingRequestParams) {
        for (int i = 0; i < pollingRequestParams.getRetryCount(); i++) {
            final Optional<Response> responseOptional = validatingRestClient.get(pollingRequestParams);

            if (responseOptional.isPresent()) {

                final Response response = responseOptional.get();
                final int status = response.getStatus();
                final String jsonResult = response.readEntity(String.class);

                if (failsCondition(response, pollingRequestParams)) {
                    throw new AssertionError(format(
                            "Failed to match response conditions from %s, after %d attempts, with status code: %s",
                            pollingRequestParams.getUrl(),
                            pollingRequestParams.getRetryCount(),
                            status));
                }

                if (failsCondition(jsonResult, pollingRequestParams)) {
                    throw new AssertionError(format(
                            "Failed to match result conditions from %s, after %d attempts, with result: %s",
                            pollingRequestParams.getUrl(),
                            pollingRequestParams.getRetryCount(),
                            jsonResult));
                }

                return jsonResult;
            }

            sleeper.sleepFor(pollingRequestParams.getDelayInMillis());
        }

        throw new AssertionError(format("Failed to get any response from '%s' after %d retries", pollingRequestParams.getUrl(), pollingRequestParams.getRetryCount()));
    }

    private boolean failsCondition(final String result, final PollingRequestParams pollingRequestParams) {
        return !pollingRequestParams.getResultCondition().test(result);
    }

    private boolean failsCondition(final Response response, final PollingRequestParams pollingRequestParams) {
        return !pollingRequestParams.getResponseCondition().test(response);
    }
}
