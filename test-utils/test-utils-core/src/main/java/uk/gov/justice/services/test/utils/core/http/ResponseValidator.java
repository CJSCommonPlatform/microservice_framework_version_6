package uk.gov.justice.services.test.utils.core.http;

import java.util.Optional;

/**
 * Validates a responseBody and status against expected status and a response body condition
 */
public class ResponseValidator {

    /**
     * Returns true if the status matches the expected status and the response body condition succeeds
     *
     * @param responseBody the response body for validating
     * @param status the status for validating
     * @param pollingRequestParams contains the expected status and response body condition
     * @return true if the status matches the expected status and the response body condition succeeds
     */
    public boolean isValid(final String responseBody, final int status, final PollingRequestParams pollingRequestParams) {
        return hasValidStatus(status, pollingRequestParams) && hasValidResponseBody(responseBody, pollingRequestParams);
    }

    /**
     * Returns true if the expected status in the request parameters matches the actual status
     *
     * @param status the actual HTTP status
     * @param pollingRequestParams contains the expected HTTP status
     * @return true if the expected status in the request parameters matches the actual status
     */
    public boolean hasValidStatus(final int status, final PollingRequestParams pollingRequestParams) {

        final Optional<Integer> expectedStatus = pollingRequestParams.getExpectedStatus();
        if(expectedStatus.isPresent()) {
            return expectedStatus.get() == status;
        }

        return true;
    }

    /**
     * Returns true if the response body matches the response body condition in the request parameters
     * @param responseBody the response body
     * @param pollingRequestParams contains the Predicate for matching against the reponse body
     * @return true if the response body matches the response body condition in the request parameters
     */
    public boolean hasValidResponseBody(final String responseBody, final PollingRequestParams pollingRequestParams) {
        return pollingRequestParams.getResposeBodyCondition().test(responseBody);
    }
}
