package uk.gov.justice.services.test.utils.core.matchers;

import javax.json.JsonValue;

import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches the Json Payload part of a JsonEnvelope. See {@link JsonEnvelopeMatcher} for usage
 * example.
 */
public class JsonEnvelopePayloadMatcher extends TypeSafeDiagnosingMatcher<JsonValue> {

    private TypeSafeDiagnosingMatcher<JsonValue> jsonValueMatcher;

    /**
     * Use {@link JsonEnvelopePayloadMatcher#payload} or {@link JsonEnvelopePayloadMatcher#payloadIsJson}
     *
     * @return the payload matcher
     */
    @Deprecated
    public static JsonEnvelopePayloadMatcher payLoad() {
        return new JsonEnvelopePayloadMatcher();
    }

    public static JsonEnvelopePayloadMatcher payload() {
        return new JsonEnvelopePayloadMatcher();
    }

    public static JsonEnvelopePayloadMatcher payloadIsJson(final Matcher<? super ReadContext> matcher) {
        return new JsonEnvelopePayloadMatcher().isJsonValue(JsonValueIsJsonMatcher.isJson(matcher));
    }

    public static JsonEnvelopePayloadMatcher payload(final TypeSafeDiagnosingMatcher<JsonValue> jsonValueMatcher) {
        return new JsonEnvelopePayloadMatcher().isJsonValue(jsonValueMatcher);
    }

    @Override
    public void describeTo(final Description description) {
        description
                .appendText("Payload ")
                .appendDescriptionOf(jsonValueMatcher)
                .appendText(" ");
    }

    @Override
    protected boolean matchesSafely(final JsonValue jsonValue, final Description description) {
        if (!jsonValueMatcher.matches(jsonValue)) {
            description.appendText("Payload ");
            jsonValueMatcher.describeMismatch(jsonValue, description);
            return false;
        }

        return true;
    }

    public JsonEnvelopePayloadMatcher isJsonValue(final TypeSafeDiagnosingMatcher<JsonValue> jsonValueMatcher) {
        this.jsonValueMatcher = jsonValueMatcher;
        return this;
    }

    public JsonEnvelopePayloadMatcher isJson(final Matcher<? super ReadContext> matcher) {
        this.isJsonValue(JsonValueIsJsonMatcher.isJson(matcher));
        return this;
    }
}