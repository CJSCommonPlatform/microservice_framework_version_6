package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.not;

import uk.gov.justice.services.test.utils.core.http.PollingRequestParams;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.util.Optional;
import java.util.concurrent.Callable;

import javax.ws.rs.core.Response;

import com.google.common.annotations.VisibleForTesting;
import com.jayway.awaitility.core.ConditionEvaluationLogger;
import com.jayway.awaitility.core.ConditionFactory;
import org.hamcrest.Matcher;

public class PollingRestClientHelper {

    private final RestClient restClient;
    private final PollingRequestParams requestParams;

    private ConditionFactory await;
    private ResponseMatcher<Response> expectedResponseMatcher;
    private Optional<ResponseMatcher<Response>> ignoreResponseMatcher = empty();

    @VisibleForTesting
    PollingRestClientHelper(final RestClient restClient, final PollingRequestParams requestParams) {
        this.requestParams = requestParams;
        this.restClient = restClient;
        await = await().with().pollInterval(1, SECONDS);
    }

    public static PollingRestClientHelper poll(final PollingRequestParams requestParams) {
        return new PollingRestClientHelper(new RestClient(), requestParams);
    }

    public PollingRestClientHelper ignoring(final ResponseMatcher<Response> responseMatcher) {
        this.ignoreResponseMatcher = Optional.of(responseMatcher);
        return this;
    }

    public void until(final ResponseMatcher<Response> responseMatcher) {
        this.expectedResponseMatcher = responseMatcher;

        await.until(new CallableRestClient(requestParams), combinedMatcher());
    }

    public PollingRestClientHelper withLogging() {
        await = await.with().conditionEvaluationListener(new ConditionEvaluationLogger());
        return this;
    }

    private Matcher<Response> combinedMatcher() {
        if (ignoreResponseMatcher.isPresent()) {
            return both(not(ignoreResponseMatcher.get())).and(expectedResponseMatcher);
        }
        return expectedResponseMatcher;
    }

    private class CallableRestClient implements Callable<Response> {
        private final PollingRequestParams requestParams;

        private CallableRestClient(final PollingRequestParams requestParams) {
            this.requestParams = requestParams;
        }

        @Override
        public Response call() throws Exception {
            return restClient.query(
                    requestParams.getUrl(),
                    requestParams.getMediaType(),
                    requestParams.getHeaders());
        }
    }


}
