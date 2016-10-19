package uk.gov.justice.services.test.utils.core.matchers;

import static javax.ws.rs.core.Response.Status.fromStatusCode;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;

public class ResponseStatusMatcher extends ResponseMatcher<Response> {

    private Status expectedStatus = null;

    public static ResponseStatusMatcher status() {
        return new ResponseStatusMatcher();
    }

    public ResponseStatusMatcher is(final Status status) {
        this.expectedStatus = status;
        return this;
    }

    @Override
    protected boolean matchesSafely(final Response response, final Description description) {
        final Status actualStatus = fromStatusCode(response.getStatus());

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
