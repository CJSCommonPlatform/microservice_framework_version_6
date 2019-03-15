package uk.gov.justice.services.test.utils.core.matchers;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.test.utils.core.matchers.EnvelopeListMatcher.*;

import uk.gov.justice.services.messaging.Envelope;

import java.util.List;
import java.util.stream.Stream;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class EnvelopeStreamMatcher<T> extends TypeSafeDiagnosingMatcher<Stream<Envelope<T>>> {

    private final Matcher[] matchers;
    private List<Envelope<T>> envelopes;

    public EnvelopeStreamMatcher(final Matcher... matchers) {
        this.matchers = matchers;
    }

    public static <T> EnvelopeStreamMatcher<T> streamContaining(final Class<T> classType, final Matcher... matchers) {
        return new EnvelopeStreamMatcher<T>(matchers);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("JsonEnvelope Stream that contains (");
        stream(matchers).forEach(description::appendDescriptionOf);
        description.appendText(")");
    }

    @Override
    protected boolean matchesSafely(final Stream<Envelope<T>> envelopeStream, final Description description) {
        if (envelopes == null) {
            envelopes = envelopeStream.collect(toList());
        }

        return new EnvelopeListMatcher<T>(matchers).matchesSafely(envelopes, description);
    }

}
