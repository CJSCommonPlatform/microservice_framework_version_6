package uk.gov.justice.domain.aggregate.matcher;

import java.util.function.Consumer;

/**
 * A combination of a rule for matching events and a consumer that can be used for consuming the
 * event if it matches.
 * @param <T> the type of event the consumer can handle
 */
public class EventMatcher<T> {

    private final Rule<T> rule;
    private final Consumer<T> consumer;

    EventMatcher(final Rule<T> rule, final Consumer<T> consumer) {
        this.rule = rule;
        this.consumer = consumer;
    }

    boolean matches(final Object value) {
        return rule.matches(value);
    }

    @SuppressWarnings("unchecked")
    Object apply(final Object value) {
        consumer.accept((T) value);
        return value;
    }
}
