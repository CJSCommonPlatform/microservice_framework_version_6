package uk.gov.justice.services.test.utils.core.http;

import static java.lang.String.format;

import uk.gov.justice.services.test.utils.core.helper.Sleeper;

import java.util.Optional;

import javax.ws.rs.core.Response;

public class PollingRestClient {

    private final ValidatingRestClient validatingRestClient;
    private final Sleeper sleeper;

    public PollingRestClient() {
        this(new ValidatingRestClient(), new Sleeper());
    }

    public PollingRestClient(final ValidatingRestClient validatingRestClient, Sleeper sleeper) {
        this.validatingRestClient = validatingRestClient;
        this.sleeper = sleeper;
    }

    public String pollUntilExpectedResponse(final PollingRequestParams pollingRequestParams) {
        for (int i = 0; i < pollingRequestParams.getRetryCount(); i++) {
            final Optional<Response> responseOptional = validatingRestClient.get(pollingRequestParams);

            if (responseOptional.isPresent()) {

                final Response response = responseOptional.get();
                final int status = response.getStatus();
                final String jsonResult = response.readEntity(String.class);

                if(failsCondition(response, pollingRequestParams))  {
                    throw new AssertionError(format(
                            "Failed to match response conditions from %s, after %d attempts, with status code: %s",
                            pollingRequestParams.getUrl(),
                            pollingRequestParams.getRetryCount(),
                            status));
                }

                if(failsCondition(jsonResult, pollingRequestParams)) {
                    throw new AssertionError(format(
                            "Failed to match result conditions from %s, after %d attempts, with result: %s",
                            pollingRequestParams.getUrl(),
                            pollingRequestParams.getRetryCount(),
                            jsonResult));
                }

                return jsonResult;
            }

            sleeper.sleepFor(pollingRequestParams.getDelayInMillis());
        }

        throw new AssertionError(format("Failed to get any response from '%s' after %d retries", pollingRequestParams.getUrl(), pollingRequestParams.getRetryCount()));
    }

    private boolean failsCondition(final String result, final PollingRequestParams pollingRequestParams) {
        return ! pollingRequestParams.getResultCondition().test(result);
    }

    private boolean failsCondition(final Response response, final PollingRequestParams pollingRequestParams) {
        return ! pollingRequestParams.getResponseCondition().test(response);
    }
}
