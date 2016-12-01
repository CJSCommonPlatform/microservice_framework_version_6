package uk.gov.justice.services.test.utils.core.matchers;

import static java.util.Optional.empty;

import uk.gov.justice.services.test.utils.core.http.ResponseData;
import uk.gov.justice.services.test.utils.core.http.RestPoller;

import java.util.Optional;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Matches the Json Payload part of a response data. See {@link RestPoller} for usage
 * example.
 */
public class ResponsePayloadMatcher extends ResponseMatcher<ResponseData> {

    private Optional<IsJson<String>> jsonMatcher = empty();
    private Optional<Matcher<String>> stringMatcher = empty();

    public static ResponsePayloadMatcher payload() {
        return new ResponsePayloadMatcher();
    }

    public ResponsePayloadMatcher isJson(final Matcher<? super ReadContext> matcher) {
        this.jsonMatcher = Optional.of(new IsJson<>(matcher));
        return this;
    }

    public ResponsePayloadMatcher that(final Matcher<String> matcher) {
        this.stringMatcher = Optional.of(matcher);
        return this;
    }

    @Override
    protected boolean matchesSafely(final ResponseData responseData, final Description description) {
        final String actualPayload = responseData.getPayload();

        if (jsonMatcher.isPresent() && !jsonMatcher.get().matches(actualPayload)) {
            describeMismatch(jsonMatcher.get(), actualPayload, description);
            return false;
        }

        if (stringMatcher.isPresent() && !stringMatcher.get().matches(actualPayload)) {
            describeMismatch(stringMatcher.get(), actualPayload, description);
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(final Description description) {
        jsonMatcher.ifPresent(matcher -> description.appendText("Payload ").appendDescriptionOf(matcher));
        stringMatcher.ifPresent(matcher -> description.appendText("Payload ").appendDescriptionOf(matcher));
    }

    private void describeMismatch(final Matcher<String> matcher, final String actualPayload, final Description description) {
        description.appendText("Payload ");
        matcher.describeMismatch(actualPayload, description);
    }
}
