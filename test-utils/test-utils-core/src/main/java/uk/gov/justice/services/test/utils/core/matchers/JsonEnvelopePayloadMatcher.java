package uk.gov.justice.services.test.utils.core.matchers;

import javax.json.JsonObject;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches the Json Payload part of a JsonEnvelope. See {@link JsonEnvelopeMatcher} for usage
 * example.
 */
public class JsonEnvelopePayloadMatcher extends TypeSafeDiagnosingMatcher<JsonObject> {

    private IsJson<Object> matcher;

    public static JsonEnvelopePayloadMatcher payLoad() {
        return new JsonEnvelopePayloadMatcher();
    }

    @Override
    public void describeTo(final Description description) {
        description
                .appendText("Payload ")
                .appendDescriptionOf(matcher)
                .appendText(" ");
    }

    public JsonEnvelopePayloadMatcher isJson(Matcher<? super ReadContext> matcher) {
        this.matcher = new IsJson<>(matcher);
        return this;
    }

    @Override
    protected boolean matchesSafely(final JsonObject jsonObject, final Description description) {
        final String jsonAsString = jsonObject.toString();

        if (!matcher.matches(jsonAsString)) {
            description.appendText("Payload ");
            matcher.describeMismatch(jsonAsString, description);
            return false;
        }

        return true;
    }
}