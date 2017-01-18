package uk.gov.justice.services.test.utils.core.helper;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;

import uk.gov.justice.services.test.utils.core.random.Generator;

import java.util.function.Function;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A Utility class for type-based testing, where a type is a high-level specification of behavior
 * that should hold for a range of data point
 *
 * @param <T> Type of the Property and must have {@link  Generator} implementation for type T Usage:
 *            typeCheck(RandomGenerator.STRING, s -> s.length() == s.toCharArray().length).withPreCondition(s
 *            -> s.length() < 5).verify(times(5));
 */
public class TypeCheck<T> {

    private final Generator<T> generator;
    private final Matcher<T> matcher;
    private Function<T, Boolean> preCondition = null;

    private TypeCheck(final Generator<T> generator, final Matcher<T> matcher) {
        requireNonNull(generator, "Generator cannot be null");
        requireNonNull(matcher, "Condition cannot be null");
        this.generator = generator;
        this.matcher = matcher;
    }

    public static <T> TypeCheck<T> typeCheck(final Generator<T> generator, final Matcher<T> matcher) {
        return new TypeCheck<>(generator, matcher);
    }

    public static <T> TypeCheck<T> typeCheck(final Generator<T> generator, final Function<T, Boolean> condition) {
        return new TypeCheck<>(generator, matcherFor(condition));
    }

    public TypeCheck<T> withPreCondition(final Function<T, Boolean> preCondition) {
        this.preCondition = preCondition;
        return this;
    }

    public void verify() {
        verify(times(1));
    }

    public void verify(Times times) {
        for (int i = 0; i < times.getNum(); i++) {
            T next = generator.next();
            if (preCondition != null && !preCondition.apply(next)) {
                i--;
            } else {
                assertThat(format("failed on attempt %s for value %s", i + 1, next), next, matcher);
            }
        }
    }

    public static class Times {
        private final int num;

        private Times(int num) {
            this.num = num;
        }

        int getNum() {
            return num;
        }

        public static Times times(int times) {
            return new Times(times);
        }
    }

    private static <T> TypeSafeMatcher<T> matcherFor(final Function<T, Boolean> condition) {
        return new TypeSafeMatcher<T>() {

            @Override
            protected boolean matchesSafely(T item) {
                return condition.apply(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("match a condition");
            }
        };
    }
}

