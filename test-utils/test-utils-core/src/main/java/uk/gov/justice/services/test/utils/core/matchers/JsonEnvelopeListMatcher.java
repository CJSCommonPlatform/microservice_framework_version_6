package uk.gov.justice.services.test.utils.core.matchers;

import static java.util.Arrays.stream;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

public class JsonEnvelopeListMatcher extends TypeSafeMatcher<List<JsonEnvelope>> {

    private final Matcher[] matchers;

    public static JsonEnvelopeListMatcher listContaining(final Matcher... matchers) {
        return new JsonEnvelopeListMatcher(matchers);
    }

    public JsonEnvelopeListMatcher(final Matcher... matchers) {
        this.matchers = matchers;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("JsonEnvelope Stream that contains: ");
        stream(matchers).forEach(description::appendDescriptionOf);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean matchesSafely(final List<JsonEnvelope> jsonEnvelopes) {
        final Matcher<Iterable<? extends List<JsonEnvelope>>> iterableMatcher = Matchers.contains(matchers);

        return iterableMatcher.matches(jsonEnvelopes);
    }
}