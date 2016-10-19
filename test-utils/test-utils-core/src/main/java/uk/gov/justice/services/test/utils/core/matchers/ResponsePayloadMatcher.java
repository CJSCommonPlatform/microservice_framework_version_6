package uk.gov.justice.services.test.utils.core.matchers;

import javax.ws.rs.core.Response;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class ResponsePayloadMatcher extends ResponseMatcher<Response> {

    private IsJson<String> matcher = null;

    public static ResponsePayloadMatcher payload() {
        return new ResponsePayloadMatcher();
    }

    public ResponsePayloadMatcher isJson(final Matcher<? super ReadContext> matcher) {
        this.matcher = new IsJson<>(matcher);
        return this;
    }

    @Override
    protected boolean matchesSafely(final Response response, final Description description) {
        final String responsePayload = response.readEntity(String.class);

        if (!matcher.matches(responsePayload)) {
            description.appendText("Payload ");
            matcher.describeMismatch(responsePayload, description);
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Payload with ")
                .appendDescriptionOf(matcher);

    }

}
