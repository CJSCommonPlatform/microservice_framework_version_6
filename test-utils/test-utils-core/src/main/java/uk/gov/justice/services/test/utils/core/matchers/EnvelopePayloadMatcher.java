package uk.gov.justice.services.test.utils.core.matchers;

import javax.json.Json;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class EnvelopePayloadMatcher<T> extends TypeSafeDiagnosingMatcher<T> {

    private TypeSafeDiagnosingMatcher<JsonValue> jsonValueMatcher;

    /**
     * Use {@link JsonEnvelopePayloadMatcher#payload} or {@link JsonEnvelopePayloadMatcher#payloadIsJson}
     *
     * @return the payload matcher
     */
    @Deprecated
    public static EnvelopePayloadMatcher payLoad() {
        return new EnvelopePayloadMatcher();
    }

    public static EnvelopePayloadMatcher payload() {
        return new EnvelopePayloadMatcher();
    }

    public static EnvelopePayloadMatcher payloadIsJson(final Matcher<? super ReadContext> matcher) {
        return new EnvelopePayloadMatcher().isJsonValue(JsonValueIsJsonMatcher.isJson(matcher));
    }

    public static EnvelopePayloadMatcher payload(final TypeSafeDiagnosingMatcher<JsonValue> jsonValueMatcher) {
        return new EnvelopePayloadMatcher().isJsonValue(jsonValueMatcher);
    }

    @Override
    public void describeTo(final Description description) {
        description
                .appendText("Payload ")
                .appendDescriptionOf(jsonValueMatcher)
                .appendText(" ");
    }

    @Override
    protected boolean matchesSafely(final T jsonValue, final Description description) {
        if (!jsonValueMatcher.matches(jsonValue)) {
            description.appendText("Payload ");
            jsonValueMatcher.describeMismatch(jsonValue, description);
            return false;
        }

        return true;
    }

    public EnvelopePayloadMatcher isJsonValue(final TypeSafeDiagnosingMatcher<JsonValue> jsonValueMatcher) {
        this.jsonValueMatcher = jsonValueMatcher;
        return this;
    }

    public EnvelopePayloadMatcher isJson(final Matcher<? super ReadContext> matcher) {
        this.isJsonValue(JsonValueIsJsonMatcher.isJson(matcher));
        return this;
    }

}
