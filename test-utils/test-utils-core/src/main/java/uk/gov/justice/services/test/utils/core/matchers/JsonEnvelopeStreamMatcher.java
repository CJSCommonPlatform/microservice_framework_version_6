package uk.gov.justice.services.test.utils.core.matchers;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.stream.Stream;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

public class JsonEnvelopeStreamMatcher extends TypeSafeMatcher<Stream<JsonEnvelope>> {

    private final Matcher[] matchers;

    public static JsonEnvelopeStreamMatcher streamContaining(final Matcher... matchers) {
        return new JsonEnvelopeStreamMatcher(matchers);
    }

    public JsonEnvelopeStreamMatcher(final Matcher... matchers) {
        this.matchers = matchers;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("JsonEnvelope Stream that contains: ");
        stream(matchers).forEach(description::appendDescriptionOf);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean matchesSafely(final Stream<JsonEnvelope> jsonEnvelopeStream) {
        final List<JsonEnvelope> jsonEnvelopes = jsonEnvelopeStream.collect(toList());
        final Matcher<Iterable<? extends List<JsonEnvelope>>> iterableMatcher = Matchers.contains(matchers);

        return iterableMatcher.matches(jsonEnvelopes);
    }
}