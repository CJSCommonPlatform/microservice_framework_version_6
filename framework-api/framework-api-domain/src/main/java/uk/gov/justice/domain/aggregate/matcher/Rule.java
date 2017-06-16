package uk.gov.justice.domain.aggregate.matcher;

import java.util.function.Consumer;

/**
 * Interface for an event matcher rule that can also build an event matcher.
 *
 * @param <T> the type of event the matchers using this rule will be able to consume
 */
public interface Rule<T> {

    /**
     * Check if the given event matches this rule.
     *
     * @param event the event to check
     * @return true if the event matches this rule
     */
    boolean matches(Object event);

    /**
     * Build an event matcher using this rule and the supplied consumer.
     *
     * @param consumer the consumer that will be used if this rule matches.
     * @return the complete event matcher
     */
    default EventMatcher<T> apply(final Consumer<T> consumer) {
        return new EventMatcher<>(this, consumer);
    }
}
