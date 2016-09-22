package uk.gov.justice.services.test.utils.core.http;

import java.util.Optional;

/**
 * Validates a responseBody and status against expected status and a response body condition
 */
public class ResponseValidator {

    public boolean isValid(final String responseBody, final int status, final PollingRequestParams pollingRequestParams) {
        return hasValidStatus(status, pollingRequestParams) && hasValidResponseBody(responseBody, pollingRequestParams);
    }

    public boolean hasValidStatus(final int status, final PollingRequestParams pollingRequestParams) {

        final Optional<Integer> expectedStatus = pollingRequestParams.getExpectedStatus();
        if(expectedStatus.isPresent()) {
            return expectedStatus.get() == status;
        }

        return true;
    }

    public boolean hasValidResponseBody(final String responseBody, final PollingRequestParams pollingRequestParams) {
        return pollingRequestParams.getResposeBodyCondition().test(responseBody);
    }
}
