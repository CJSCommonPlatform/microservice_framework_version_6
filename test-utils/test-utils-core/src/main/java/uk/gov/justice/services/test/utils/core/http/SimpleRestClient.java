package uk.gov.justice.services.test.utils.core.http;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.util.Optional;

import javax.ws.rs.core.Response;

public class SimpleRestClient {

    private final RestClient restClient;

    public SimpleRestClient() {
        this(new RestClient());
    }

    public SimpleRestClient(final RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<Response> get(final PollingRequestParams pollingRequestParams) {

        final Response response = restClient.query(
                pollingRequestParams.getUrl(),
                pollingRequestParams.getMediaType(),
                pollingRequestParams.getHeaders());
        final String jsonResult = response.readEntity(String.class);

        if (failsValidation(pollingRequestParams, response, jsonResult)) {
            return empty();
        }

        return of(response);
    }

    private boolean failsValidation(final PollingRequestParams pollingRequestParams, final Response response, final String jsonResult) {
        return responseFailsCondition(response, pollingRequestParams) || jsonFailsCondition(jsonResult, pollingRequestParams);
    }

    private boolean jsonFailsCondition(final String jsonResult, final PollingRequestParams pollingRequestParams) {
        return !pollingRequestParams.getResultCondition().test(jsonResult);
    }

    private boolean responseFailsCondition(final Response response, final PollingRequestParams pollingRequestParams) {
        return !pollingRequestParams.getResponseCondition().test(response);
    }
}
