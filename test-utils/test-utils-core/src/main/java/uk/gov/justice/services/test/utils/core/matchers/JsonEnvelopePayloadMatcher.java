package uk.gov.justice.services.test.utils.core.matchers;

import javax.json.JsonObject;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class JsonEnvelopePayloadMatcher extends TypeSafeMatcher<JsonObject> {

    private IsJson<Object> matcher;

    public static JsonEnvelopePayloadMatcher payLoad() {
        return new JsonEnvelopePayloadMatcher();
    }

    @Override
    public void describeTo(final Description description) {
        description
                .appendText("Json payload (")
                .appendDescriptionOf(matcher)
                .appendText(") ");
    }

    public JsonEnvelopePayloadMatcher isJson(Matcher<? super ReadContext> matcher) {
        this.matcher = new IsJson<>(matcher);
        return this;
    }

    @Override
    protected boolean matchesSafely(final JsonObject jsonObject) {
        final String jsonAsString = jsonObject.toString();
        return matcher.matches(jsonAsString);
    }
}