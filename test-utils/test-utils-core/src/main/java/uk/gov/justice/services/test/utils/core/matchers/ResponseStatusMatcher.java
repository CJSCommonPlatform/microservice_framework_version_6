package uk.gov.justice.services.test.utils.core.matchers;

import javax.ws.rs.core.Response;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ResponseStatusMatcher extends TypeSafeMatcher<Integer> {

    private final Response.Status status;

    public ResponseStatusMatcher(final Response.Status status) {
        this.status = status;
    }

    @Override
    protected boolean matchesSafely(final Integer statusCode) {
        return status.getStatusCode() == statusCode;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("http status of " + status.getStatusCode());
    }

    public static Matcher<Integer> isStatus(final Response.Status status) {
        return new ResponseStatusMatcher(status);
    }
}
