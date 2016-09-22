package uk.gov.justice.services.test.utils.core.http;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.util.Optional;

import javax.ws.rs.core.Response;

public class ValidatingRestClient {

    private final RestClient restClient;

    public ValidatingRestClient() {
        this(new RestClient());
    }

    public ValidatingRestClient(final RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<ResponseDetails> get(final PollingRequestParams pollingRequestParams) {

        final Response response = restClient.query(
                pollingRequestParams.getUrl(),
                pollingRequestParams.getMediaType(),
                pollingRequestParams.getHeaders());


        final int status = response.getStatus();
        final String responseBody = response.readEntity(String.class);

        if (failsValidation(pollingRequestParams, status, responseBody)) {
            return empty();
        }

        return of(new ResponseDetails(status, responseBody));
    }

    private boolean failsValidation(final PollingRequestParams pollingRequestParams, final int status, final String jsonResult) {
        return hasIncorrectStatus(status, pollingRequestParams) || jsonFailsCondition(jsonResult, pollingRequestParams);
    }

    private boolean hasIncorrectStatus(final int status, final PollingRequestParams pollingRequestParams) {

        final Optional<Integer> expectedStatus = pollingRequestParams.getExpectedStatus();
        if(expectedStatus.isPresent()) {
            final boolean b = expectedStatus.get() != status;
            return b;
        }

        return false;
    }

    private boolean jsonFailsCondition(final String jsonResult, final PollingRequestParams pollingRequestParams) {
        final boolean test = pollingRequestParams.getResultCondition().test(jsonResult);
        return !test;
    }
}
