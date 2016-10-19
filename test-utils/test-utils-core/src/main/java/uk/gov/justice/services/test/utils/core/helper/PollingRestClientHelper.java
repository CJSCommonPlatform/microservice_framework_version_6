package uk.gov.justice.services.test.utils.core.helper;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.fromStatusCode;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.not;

import uk.gov.justice.services.test.utils.core.http.PollingRequestParams;
import uk.gov.justice.services.test.utils.core.http.ResponseData;
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
    private Matcher<ResponseData> expectedResponseMatcher;
    private Optional<Matcher<ResponseData>> ignoredResponseMatcher = empty();

    @VisibleForTesting
    PollingRestClientHelper(final RestClient restClient, final PollingRequestParams requestParams) {
        this.requestParams = requestParams;
        this.restClient = restClient;
        await = await().with().pollInterval(1, SECONDS).with().timeout(5, SECONDS);
    }

    public static PollingRestClientHelper poll(final PollingRequestParams requestParams) {
        return new PollingRestClientHelper(new RestClient(), requestParams);
    }

    public PollingRestClientHelper ignoring(final Matcher<ResponseData>... matchers) {
        this.ignoredResponseMatcher = Optional.of(allOf(matchers));
        return this;
    }

    public void until(final Matcher<ResponseData>... matchers) {
        expectedResponseMatcher = allOf(matchers);

        await.until(new CallableRestClient(requestParams), combinedMatcher());
    }

    public PollingRestClientHelper withLogging() {
        await = await.with().conditionEvaluationListener(new ConditionEvaluationLogger());
        return this;
    }

    private Matcher<ResponseData> combinedMatcher() {
        if (ignoredResponseMatcher.isPresent()) {
            return both(not(ignoredResponseMatcher.get())).and(expectedResponseMatcher);
        }
        return expectedResponseMatcher;
    }

    private class CallableRestClient implements Callable<ResponseData> {
        private final PollingRequestParams requestParams;

        private CallableRestClient(final PollingRequestParams requestParams) {
            this.requestParams = requestParams;
        }

        @Override
        public ResponseData call() throws Exception {
            final Response response = restClient.query(
                    requestParams.getUrl(),
                    requestParams.getMediaType(),
                    requestParams.getHeaders());

            return new ResponseData(fromStatusCode(response.getStatus()), response.readEntity(String.class));
        }
    }

}
