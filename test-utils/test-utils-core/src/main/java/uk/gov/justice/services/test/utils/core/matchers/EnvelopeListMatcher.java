package uk.gov.justice.services.test.utils.core.matchers;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import uk.gov.justice.services.messaging.Envelope;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.collection.IsIterableContainingInOrder;

public class EnvelopeListMatcher<T> extends TypeSafeDiagnosingMatcher<List<Envelope<T>>> {

    private final Matcher[] matchers;

    public EnvelopeListMatcher(final Matcher... matchers) {
        this.matchers = matchers;
    }

    public static EnvelopeListMatcher listContaining(final Matcher... matchers) {
        return new EnvelopeListMatcher(matchers);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("JsonEnvelope List that contains: ");
        stream(matchers).forEach(description::appendDescriptionOf);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean matchesSafely(final List<Envelope<T>> envelopes, final Description description) {
        final IsIterableContainingInOrder containingInOrder = new IsIterableContainingInOrder(asList(matchers));

        if (!containingInOrder.matches(envelopes)) {
            containingInOrder.describeMismatch(envelopes, description);
            return false;
        }

        return true;
    }
}
