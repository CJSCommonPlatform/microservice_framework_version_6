package uk.gov.justice.services.test.utils.core.http;

import static com.sun.tools.doclint.Entity.Mu;
import static java.util.Collections.singletonList;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParams.DEFAULT_DELAY_MILLIS;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParams.DEFAULT_RETRY_COUNT;

import java.util.Map;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class PollingRequestParamsBuilder {

    private final String url;
    private final String mediaType;

    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    private Predicate<Response> responseCondition = new DefaultCondition<>();
    private Predicate<String> resultCondition = new DefaultCondition<>();
    private long delayInMillis = DEFAULT_DELAY_MILLIS;
    private int retryCount = DEFAULT_RETRY_COUNT;



    public PollingRequestParamsBuilder(final String url, final String mediaType) {
        this.url = url;
        this.mediaType = mediaType;
    }

    public PollingRequestParamsBuilder withResponseCondition(final Predicate<Response> responseCondition) {
        this.responseCondition = responseCondition;
        return this;
    }

    public PollingRequestParamsBuilder withResultCondition(final Predicate<String> resultCondition) {
        this.resultCondition = resultCondition;
        return this;
    }

    public PollingRequestParamsBuilder withDelayInMillis(final long delayInMillis) {
        this.delayInMillis = delayInMillis;
        return this;
    }

    public PollingRequestParamsBuilder withRetryCount(final int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public PollingRequestParamsBuilder withHeader(final String name, final Object value) {
        headers.put(name, singletonList(value));
        return this;
    }

    public PollingRequestParamsBuilder withHeaders(final Map<String, Object> headers) {
        this.headers = new MultivaluedHashMap<>(headers);
        return this;
    }

    public PollingRequestParamsBuilder withExpectedJsonResponseValues(final Map<String, String> values) {
        this.resultCondition = new ExpectedJsonValuesResultCondition(values);
        return this;
    }

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

    private static class DefaultCondition<T> implements Predicate<T> {

        @Override
        public boolean test(final T response) {
            return true;
        }
    }
}
