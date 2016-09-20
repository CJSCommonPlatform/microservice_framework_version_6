package uk.gov.justice.services.test.utils.core.http;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.util.Optional;

import javax.ws.rs.core.Response;

/**
 * A RestClient to query rest endpoints and validates it's response.
 */
public class ValidatingRestClient {

    private final RestClient restClient;

    public ValidatingRestClient() {
        this(new RestClient());
    }

    public ValidatingRestClient(final RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Queries the rest endpoint and validates the response and jsonResult based on the ResponseCondition and ResultCondition.
     * If the validation is successful then returns the <code>Response</code> otherwise returns <code>Optional.Empty</code>.
     * <p>
     * The validation predicates are configured using <code>PollingRequestParams</code>.
     *
     * @param pollingRequestParams all parameters for polling the end point. Best created using the
     * @return <code>Optional<Response></code> If the validation is successful then returns the <code>Response</code>,
     * otherwise returns <code>Optional.Empty</code>.
     * @See <code>PollingRequestParams</code> and <code>PollingRequestParamsBuilder</code>.
     * @See PollingRequestParamsBuilder
     */
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
