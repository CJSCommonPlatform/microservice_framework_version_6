package uk.gov.justice.services.test.utils.core.http;

import static java.lang.String.format;

import uk.gov.justice.services.test.utils.core.helper.Sleeper;

import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;

/**
 * Client for polling a rest endpoint a configured number of times with a configured wait time
 * between each poll.
 *
 * Can accept response body Predicate for validating the rest response body and an expected status
 */
public class PollingRestClient {

    private final ValidatingRestClient validatingRestClient;
    private final Sleeper sleeper;
    private final ResponseValidator responseValidator;

    /**
     * Constructs a fully functioning PollingRestClient
     */
    public PollingRestClient() {
        this(new ValidatingRestClient(), new Sleeper(), new ResponseValidator());
    }

    @VisibleForTesting
    PollingRestClient(final ValidatingRestClient validatingRestClient, final Sleeper sleeper, final ResponseValidator responseValidator) {
        this.validatingRestClient = validatingRestClient;
        this.sleeper = sleeper;
        this.responseValidator = responseValidator;
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
     * Response body Condition:
     *
     * To allow waiting until a particular value is updated, then there is a Predicate that can
     * be added to the builder which validates the response body as a String: the Result condition.
     *
     * This is best added using the PollingRequestParamsBuilder:
     *
     * <pre><blockquote>
     *
     *     final PollingRequestParams pollingRequestParams =
     *          new PollingRequestParamsBuilder(url, mediaType)
     *              .withResponseBodyCondition(json -> json.equals("my-json"))
     *          .build();
     *
     * </blockquote></pre>
     *
     * Expeced status:
     *
     * An optional http status which will be compared against the actual response status
     *
     * If this is set then the actual status returned by the call will be matched against the
     * expected status in the request parameters
     *
     * This is best added using the PollingRequestParamsBuilder:
     *
     * <pre><blockquote>
     *
     *     final PollingRequestParams pollingRequestParams =
     *          new PollingRequestParamsBuilder(url, mediaType)
     *              .withExpectedResponseStatus(200)
     *          .build();
     *
     * </blockquote></pre>
     *
     * @param pollingRequestParams all parameters for polling the end point. Best created using the
     *                             @See PollingRequestParamsBuilder
     * @return the response body as a String
     * @throws AssertionError if the request validation fails or no response is found after the
     * specified number of retires
     *
     */
    public String pollUntilExpectedResponse(final PollingRequestParams pollingRequestParams) {
        for (int i = 0; i < pollingRequestParams.getRetryCount(); i++) {
            final Optional<ResponseDetails> responseOptional = validatingRestClient.get(pollingRequestParams);

            if (responseOptional.isPresent()) {

                final ResponseDetails responseDetails = responseOptional.get();
                final int status = responseDetails.getStatus();
                final String responseBody = responseDetails.getResponseBody();

                if (!responseValidator.hasValidResponseBody(responseBody, pollingRequestParams)) {
                    throw new AssertionError(format(
                            "Failed to match result conditions from %s, after %d attempts, with result: %s",
                            pollingRequestParams.getUrl(),
                            pollingRequestParams.getRetryCount(),
                            responseBody));
                }

                if (!responseValidator.hasValidStatus(status, pollingRequestParams)) {
                    //noinspection OptionalGetWithoutIsPresent
                    throw new AssertionError(format(
                            "Incorrect http response status received from %s. Expected %d, received %d",
                            pollingRequestParams.getUrl(),
                            pollingRequestParams.getExpectedStatus().get(),
                            status));
                }

                return responseBody;
            }

            sleeper.sleepFor(pollingRequestParams.getDelayInMillis());
        }

        throw new AssertionError(format("Failed to get any response from '%s' after %d retries", pollingRequestParams.getUrl(), pollingRequestParams.getRetryCount()));
    }


}
