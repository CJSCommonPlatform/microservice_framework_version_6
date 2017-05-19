package uk.gov.justice.services.test.utils.core.http;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParams.DEFAULT_DELAY_MILLIS;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParams.DEFAULT_RETRY_COUNT;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

/**
 * Builder for creating PollingRequestParameters. Expects a url and a media type. All other
 * required parameters for polling a request point and validating its response are given as defaults.
 */
public class PollingRequestParamsBuilder {

    private final String url;
    private final String mediaType;

    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    private Predicate<String> resultCondition = json -> true;
    private Optional<Status> expectedResponseStatus = empty();
    private long delayInMillis = DEFAULT_DELAY_MILLIS;
    private int retryCount = DEFAULT_RETRY_COUNT;

    /**
     * Constructs a PollingRequestParamsBuilder
     *
     * @param url the url of the rest endpoint
     * @param mediaType the media type
     */
    public PollingRequestParamsBuilder(final String url, final String mediaType) {
        this.url = url;
        this.mediaType = mediaType;
    }


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
     * Adds an expected HTTP status that should match the status in the response. If not
     * set then the status will not be checked
     *
     * @param status the expected HTTP status that should be returned from the server
     * @return this
     */
    public PollingRequestParamsBuilder withExpectedResponseStatus(final Status status) {
        expectedResponseStatus = of(status);
        return this;
    }

    /**
     * Adds a condition for verifying the response body from a call to a rest endpoint. By default
     * allows all.
     *
     * To use:
     * <pre>
     *  {@code
     *
     *      new PollingRequestParamsBuilder()
     *          .withResultCondition(json -> json.equals("my-json"))
     *          .build();
     *
     *  }
     * </pre>
     *
     * @param resultCondition the result condition
     * @return the PollingRequestParamsBuilder
     */
    public PollingRequestParamsBuilder withResponseBodyCondition(final Predicate<String> resultCondition) {
        this.resultCondition = resultCondition;
        return this;
    }

    /**
     * Adds a pre configured result condition, which verified that all of the names/values exist
     * in the response body json
     *
     * {@link ExpectedJsonValuesResultCondition}
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
                resultCondition,
                delayInMillis,
                retryCount,
                expectedResponseStatus
        );
    }
}
