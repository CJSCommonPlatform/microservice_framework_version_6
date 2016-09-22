package uk.gov.justice.services.test.utils.core.http;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class PollingRequestParams {

    public static final int DEFAULT_RETRY_COUNT = 5;
    public static final long DEFAULT_DELAY_MILLIS = 1000L;

    private final String url;
    private final String mediaType;
    private final MultivaluedMap<String, Object> headers;

    private final Predicate<String> resposeBodyCondition;
    private final long delayInMillis;
    private final int retryCount;
    private final Optional<Status> expectedStatus;

    public PollingRequestParams(
            final String url,
            final String mediaType,
            final MultivaluedMap<String, Object> headers,
            final Predicate<String> resposeBodyCondition,
            final long delayInMillis,
            final int retryCount,
            final Optional<Status> expectedStatus) {
        this.url = url;
        this.mediaType = mediaType;
        this.headers = headers;
        this.resposeBodyCondition = resposeBodyCondition;
        this.delayInMillis = delayInMillis;
        this.retryCount = retryCount;
        this.expectedStatus = expectedStatus;
    }

    /**
     * @return The url of the rest  end point
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the media/content type of the rest call
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * @return the expected status of the response
     */
    public Optional<Status> getExpectedStatus() {
        return expectedStatus;
    }

    /**
     * @return a map of header names/values for adding to the request
     */
    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    /**
     * @return a predicate that can be used to verify the response body.
     */
    public Predicate<String> getResposeBodyCondition() {
        return resposeBodyCondition;
    }

    /**
     * @return the delay time in milliseconds between each attempted request
     */
    public long getDelayInMillis() {
        return delayInMillis;
    }

    /**
     * @return the number of retries
     */
    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PollingRequestParams that = (PollingRequestParams) o;
        return getDelayInMillis() == that.getDelayInMillis() &&
                getRetryCount() == that.getRetryCount() &&
                Objects.equals(getUrl(), that.getUrl()) &&
                Objects.equals(getMediaType(), that.getMediaType()) &&
                Objects.equals(getHeaders(), that.getHeaders()) &&
                Objects.equals(getResposeBodyCondition(), that.getResposeBodyCondition()) &&
                Objects.equals(getExpectedStatus(), that.getExpectedStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getUrl(),
                getMediaType(),
                getHeaders(),
                getResposeBodyCondition(),
                getDelayInMillis(),
                getRetryCount(),
                getExpectedStatus());
    }

    @Override
    public String toString() {
        return "PollingRequestParams{" +
                "url='" + url + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", headers=" + headers +
                ", resultCondition=" + resposeBodyCondition +
                ", delayInMillis=" + delayInMillis +
                ", retryCount=" + retryCount +
                ", expectedStatus=" + expectedStatus +
                '}';
    }
}
