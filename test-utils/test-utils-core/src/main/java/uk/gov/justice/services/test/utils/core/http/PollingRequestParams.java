package uk.gov.justice.services.test.utils.core.http;

import static java.util.Objects.hash;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class PollingRequestParams {

    public static final int DEFAULT_RETRY_COUNT = 5;
    public static final long DEFAULT_DELAY_MILLIS = 1000L;

    private final String url;
    private final String mediaType;
    private final MultivaluedMap<String, Object> headers;

    private final Predicate<String> resultCondition;
    private final long delayInMillis;
    private final int retryCount;
    private final Optional<Integer> expectedStatus;

    public PollingRequestParams(
            final String url,
            final String mediaType,
            final MultivaluedMap<String, Object> headers,
            final Predicate<String> resultCondition,
            final long delayInMillis,
            final int retryCount,
            final Optional<Integer> expectedStatus) {
        this.url = url;
        this.mediaType = mediaType;
        this.headers = headers;
        this.resultCondition = resultCondition;
        this.delayInMillis = delayInMillis;
        this.retryCount = retryCount;
        this.expectedStatus = expectedStatus;
    }

    public String getUrl() {
        return url;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Optional<Integer> getExpectedStatus() {
        return expectedStatus;
    }

    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    public Predicate<String> getResultCondition() {
        return resultCondition;
    }

    public long getDelayInMillis() {
        return delayInMillis;
    }

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
                Objects.equals(getResultCondition(), that.getResultCondition()) &&
                Objects.equals(getExpectedStatus(), that.getExpectedStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getUrl(),
                getMediaType(),
                getHeaders(),
                getResultCondition(),
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
                ", resultCondition=" + resultCondition +
                ", delayInMillis=" + delayInMillis +
                ", retryCount=" + retryCount +
                ", expectedStatus=" + expectedStatus +
                '}';
    }
}
