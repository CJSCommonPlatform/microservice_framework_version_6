package uk.gov.justice.services.test.utils.core.matchers;

import uk.gov.justice.services.test.utils.core.helper.PollingRestClientHelper;
import uk.gov.justice.services.test.utils.core.http.ResponseData;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Matches the Json Payload part of a response data. See {@link PollingRestClientHelper} for usage
 * example.
 */
public class ResponsePayloadMatcher extends ResponseMatcher<ResponseData> {

    private IsJson<String> matcher = null;

    public static ResponsePayloadMatcher payload() {
        return new ResponsePayloadMatcher();
    }

    public ResponsePayloadMatcher isJson(final Matcher<? super ReadContext> matcher) {
        this.matcher = new IsJson<>(matcher);
        return this;
    }

    @Override
    protected boolean matchesSafely(final ResponseData responseData, final Description description) {
        final String actualPayload = responseData.getPayload();

        if (!matcher.matches(actualPayload)) {
            description.appendText("Payload ");
            matcher.describeMismatch(actualPayload, description);
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Payload ")
                .appendDescriptionOf(matcher);

    }

}
