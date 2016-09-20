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

    public Optional<Response> get(PollingRequestParams pollingRequestParams) {

        final Response response = restClient.query(
                pollingRequestParams.getUrl(),
                pollingRequestParams.getMediaType(),
                pollingRequestParams.getHeaders());
        final String result = response.readEntity(String.class);

        if (pollingRequestParams.getResponseCondition().test(response) && pollingRequestParams.getResultCondition().test(result)) {
            return of(response);
        }

        return empty();
    }
}
