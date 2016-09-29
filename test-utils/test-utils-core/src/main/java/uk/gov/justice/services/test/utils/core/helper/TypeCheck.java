package uk.gov.justice.services.test.utils.core.helper;

import uk.gov.justice.services.test.utils.core.random.Generator;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * A Utility class for type-based testing, where a type is a high-level specification of behavior that should hold for a range of data point
 *
 * @param <T> Type of the Property and must have {@link  Generator} implementation for type T
 *            Usage: aTypeCheck(RandomGenerator.STRING, s -> s.length() == s.toCharArray().length).numberOfTimes(10).preCondition(s -> s.length() < 5).verify();
 */
public class TypeCheck<T> {

    private final Generator<T> generator;
    private final Function<T, Boolean> condition;
    private Function<T, Boolean> preCondition = null;
    private int numberOfTimes;

    private TypeCheck(Generator<T> generator, Function<T, Boolean> condition) {
        requireNonNull(generator, "Generator cannot be null");
        requireNonNull(condition, "Condition cannot be null");
        this.generator = generator;
        this.condition = condition;
    }

    public static <T> TypeCheck<T> aTypeCheck(Generator<T> generator, Function<T, Boolean> condition) {
        return new TypeCheck<T>(generator, condition);
    }

    public TypeCheck<T> numberOfTimes(int numberOfTimes) {
        this.numberOfTimes = numberOfTimes;
        return this;
    }

    public TypeCheck<T> preCondition(Function<T, Boolean> preCondition) {
        this.preCondition = preCondition;
        return this;
    }

    public void verify() {
        for (int i = 0; i < numberOfTimes; i++) {
            T next = generator.next();
            if (preCondition != null && !preCondition.apply(next)) {
                i--;
            } else {
                assertThat(condition.apply(next), is(true));
            }
        }
    }
}