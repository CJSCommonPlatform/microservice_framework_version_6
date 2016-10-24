package uk.gov.justice.services.test.utils.core.matchers;

import uk.gov.justice.services.test.utils.core.helper.PollingRestClientHelper;
import uk.gov.justice.services.test.utils.core.http.ResponseData;

import javax.ws.rs.core.Response.Status;

import org.hamcrest.Description;

/**
 * Matches the HTTP status part of a response data. See {@link PollingRestClientHelper} for usage
 * example.
 */
public class ResponseStatusMatcher extends ResponseMatcher<ResponseData> {

    private Status expectedStatus = null;

    public static ResponseStatusMatcher status() {
        return new ResponseStatusMatcher();
    }

    public ResponseStatusMatcher is(final Status status) {
        this.expectedStatus = status;
        return this;
    }

    @Override
    protected boolean matchesSafely(final ResponseData responseData, final Description description) {
        final Status actualStatus = responseData.getStatus();

        if (actualStatus != expectedStatus) {
            description.appendText("got ").appendValue(actualStatus);
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Status ").appendValue(expectedStatus);
    }

}
