package uk.gov.justice.services.test.utils.core.matchers;

import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches an empty Stream.
 *
 * <pre>
 *  {@code
 *         assertThat(Stream.empty(), isEmptyStream());
 * }
 * </pre>
 */
public class EmptyStreamMatcher extends TypeSafeDiagnosingMatcher<Stream<?>> {

    private Optional<Long> streamSize = Optional.empty();

    public static EmptyStreamMatcher isEmptyStream() {
        return new EmptyStreamMatcher();
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(" empty stream ");
    }

    @Override
    protected boolean matchesSafely(final Stream<?> stream, final Description description) {
        if (!streamSize.isPresent()) {
            streamSize = Optional.of(stream.count());
        }

        final boolean streamIsNotEmpty = !streamSize.get().equals(0L);
        if (streamIsNotEmpty) {
            description.appendText("stream with ").appendValue(streamSize).appendText(" elements");
            return false;
        }

        return true;
    }
}
