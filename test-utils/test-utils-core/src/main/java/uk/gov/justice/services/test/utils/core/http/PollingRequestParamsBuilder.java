package uk.gov.justice.services.test.utils.core.http;

import static java.util.Collections.singletonList;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParams.DEFAULT_DELAY_MILLIS;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParams.DEFAULT_RETRY_COUNT;

import java.util.Map;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Builder for creating PollingRequestParameters. Expects a url and a media type. All other
 * required parameters for polling a request point are given as defaults.
 */
public class PollingRequestParamsBuilder {

    private final String url;
    private final String mediaType;

    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    private Predicate<Response> responseCondition = new DefaultCondition<>();
    private Predicate<String> resultCondition = new DefaultCondition<>();
    private long delayInMillis = DEFAULT_DELAY_MILLIS;
    private int retryCount = DEFAULT_RETRY_COUNT;

    /**
     * Convenience method for creating a new PollingRequestParamsBuilder
     * @param url the url of the rest endpoint
     * @param mediaType the media type
     *
     * @return a new PollingRequestParamsBuilder
     */
    public static PollingRequestParamsBuilder pollingRequestParams(final String url, final String mediaType) {
        return new PollingRequestParamsBuilder(url, mediaType);
    }

    public PollingRequestParamsBuilder(final String url, final String mediaType) {
        this.url = url;
        this.mediaType = mediaType;
    }

    /**
     * Overrides the delay between polls. If not specified a default of 1 second is used.
     * @param delayInMillis the delay betweeen polls in milliseconds
     * @return this
     */
    public PollingRequestParamsBuilder withDelayInMillis(final long delayInMillis) {
        this.delayInMillis = delayInMillis;
        return this;
    }

    /**
     * Overides the number of retries. If not specifed a default of 5 times is used.
     * @param retryCount the number of retries
     * @return this
     */
    public PollingRequestParamsBuilder withRetryCount(final int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    /**
     * Adds a header to the rest request.
     * @param name the name of the header
     * @param value the value of the header
     * @return this
     */
    public PollingRequestParamsBuilder withHeader(final String name, final Object value) {
        headers.put(name, singletonList(value));
        return this;
    }

    /**
     * Adds a map of headers to the rest request.
     * @param headers a map of header names to header values
     * @return this
     */
    public PollingRequestParamsBuilder withHeaders(final Map<String, Object> headers) {
        this.headers.putAll(new MultivaluedHashMap<>(headers));
        return this;
    }

    /**
     * Adds a condition for verifying the Response from the rest endpoint. By default allows all.
     *
     * To use:
     *      <pre><blockquote>
     *
     *      new PollingRequestParamsBuilder()
     *          .withResponseCondition(response -> response.getStatus() == 200)
     *          .build();
     *
     *      </blockquote></pre>
     *
     * @param responseCondition a Predicate for verifying the rest Response
     * @return this
     */
    public PollingRequestParamsBuilder withResponseCondition(final Predicate<Response> responseCondition) {
        this.responseCondition = responseCondition;
        return this;
    }

    /**
     * Adds a condition for verifying the response body from a call to a rest endpoint. By default allows all.
     *
     * To use:
     *      <pre><blockquote>
     *
     *      new PollingRequestParamsBuilder()
     *          .withResultCondition(json -> json.equals("my-json"))
     *          .build();
     *
     *      </blockquote></pre>
     *
     * @param resultCondition
     * @return
     */
    public PollingRequestParamsBuilder withResultCondition(final Predicate<String> resultCondition) {
        this.resultCondition = resultCondition;
        return this;
    }

    /**
     * Adds a pre configured result condition, which verified that all of the names/values exist
     * in the response body json
     *
     * @See ExpectedJsonValuesResultCondition
     *
     * @param values a Map of property names to values that should exist in the result json
     * @return this
     */
    public PollingRequestParamsBuilder withExpectedJsonResponseValues(final Map<String, String> values) {
        this.resultCondition = new ExpectedJsonValuesResultCondition(values);
        return this;
    }

    /**
     * Builds PollingRequestParameters from the supplied values/defaults
     * @return PollingRequestParameters
     */
    public PollingRequestParams build() {
        return new PollingRequestParams(
                url,
                mediaType,
                headers,
                responseCondition,
                resultCondition,
                delayInMillis,
                retryCount
        );
    }

    /**
     * Default condition to allow all in verification. Used as a default for the
     * Response condition and the result condition.
     * @param <T> either Response or String
     */
    private static class DefaultCondition<T> implements Predicate<T> {

        @Override
        public boolean test(final T response) {
            return true;
        }
    }
}
