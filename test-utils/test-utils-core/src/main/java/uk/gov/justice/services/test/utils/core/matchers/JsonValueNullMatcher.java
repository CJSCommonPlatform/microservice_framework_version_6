package uk.gov.justice.services.test.utils.core.matchers;

import javax.json.JsonValue;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class JsonValueNullMatcher extends TypeSafeDiagnosingMatcher<JsonValue> {

    public static JsonValueNullMatcher isJsonValueNull() {
        return new JsonValueNullMatcher();
    }

    @Override
    protected boolean matchesSafely(final JsonValue jsonValue, final Description description) {
        if (!JsonValue.NULL.equals(jsonValue)) {
            description.appendText(jsonValue.toString());
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("JsonValue.NULL");
    }
}