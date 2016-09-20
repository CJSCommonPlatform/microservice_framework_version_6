package uk.gov.justice.services.test.utils.core.http;

import static java.util.Objects.hash;

import java.util.Objects;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class PollingRequestParams {

    public static final int DEFAULT_RETRY_COUNT = 5;
    public static final long DEFAULT_DELAY_MILLIS = 1000L;

    private final String url;
    private final String mediaType;
    private final MultivaluedMap<String, Object> headers;

    private final Predicate<Response> responseCondition;
    private final Predicate<String> resultCondition;
    private final long delayInMillis;
    private final int retryCount;

    public PollingRequestParams(
            final String url,
            final String mediaType,
            final MultivaluedMap<String, Object> headers,
            final Predicate<Response> responseCondition,
            final Predicate<String> resultCondition,
            final long delayInMillis,
            final int retryCount) {
        this.url = url;
        this.mediaType = mediaType;
        this.headers = headers;
        this.responseCondition = responseCondition;
        this.resultCondition = resultCondition;
        this.delayInMillis = delayInMillis;
        this.retryCount = retryCount;
    }

    public String getUrl() {
        return url;
    }

    public String getMediaType() {
        return mediaType;
    }

    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    public Predicate<Response> getResponseCondition() {
        return responseCondition;
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
        final PollingRequestParams pollingRequestParams = (PollingRequestParams) o;
        return getDelayInMillis() == pollingRequestParams.getDelayInMillis() &&
                getRetryCount() == pollingRequestParams.getRetryCount() &&
                Objects.equals(getUrl(), pollingRequestParams.getUrl()) &&
                Objects.equals(getMediaType(), pollingRequestParams.getMediaType()) &&
                Objects.equals(getHeaders(), pollingRequestParams.getHeaders()) &&
                Objects.equals(getResponseCondition(), pollingRequestParams.getResponseCondition()) &&
                Objects.equals(getResultCondition(), pollingRequestParams.getResultCondition());
    }

    @Override
    public int hashCode() {
        return hash(
                getUrl(),
                getMediaType(),
                getHeaders(),
                getResponseCondition(),
                getResultCondition(),
                getDelayInMillis(),
                getRetryCount());
    }

    @Override
    public String toString() {
        return "RestStuff{" +
                "url='" + url + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", headers=" + headers +
                ", responseCondition=" + responseCondition +
                ", resultCondition=" + resultCondition +
                ", delayInMillis=" + delayInMillis +
                ", retryCount=" + retryCount +
                '}';
    }
}
