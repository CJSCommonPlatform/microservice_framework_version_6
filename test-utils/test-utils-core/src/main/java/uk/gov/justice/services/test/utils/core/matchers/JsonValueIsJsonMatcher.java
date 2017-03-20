package uk.gov.justice.services.test.utils.core.matchers;

import static java.lang.String.format;

import javax.json.JsonValue;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class JsonValueIsJsonMatcher extends TypeSafeDiagnosingMatcher<JsonValue> {

    private IsJson<Object> matcher;

    public JsonValueIsJsonMatcher(final Matcher<? super ReadContext> matcher) {
        super();
        this.matcher = new IsJson<>(matcher);
    }

    public static JsonValueIsJsonMatcher isJson(final Matcher<? super ReadContext> matcher) {
        return new JsonValueIsJsonMatcher(matcher);
    }

    @Override
    protected boolean matchesSafely(final JsonValue jsonValue, final Description description) {
        if (!JsonValue.ValueType.OBJECT.equals(jsonValue.getValueType())) {
            description.appendText(format("%s is not a JsonObject", jsonValue));
            return false;
        }

        final String jsonAsString = jsonValue.toString();

        if (!matcher.matches(jsonAsString)) {
            matcher.describeMismatch(jsonAsString, description);
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendDescriptionOf(matcher);
    }
}