package uk.gov.justice.domain.aggregate.matcher;

import static java.lang.String.format;

/**
 * Helper class for building code for applying events in aggregates without if statements and class
 * casts. Using this class allows aggregates to be written like this:
 *
 * <pre>
 * {@code
 *     public Object apply(final Object event) {
 *         return match(event).with(
 *             when(RecipeAdded.class).apply(x -> recipeId = x.getRecipeId()),
 *             when(RecipeNameUpdated.class).apply(x -> name = x.getName()));
 *     }
 * }
 * </pre>
 */
public class EventSwitcher {

    private Object event;

    /**
     * Private constructor. Use the {@code match} method for creating match switcher.
     *
     * @param event the event to be matched
     */
    private EventSwitcher(final Object event) {
        this.event = event;
    }

    /**
     * Create a new event switcher to match the event provided.
     *
     * @param event the event to match
     * @return an event switcher
     */
    public static EventSwitcher match(final Object event) {
        return new EventSwitcher(event);
    }

    /**
     * Create a new event matcher rule for matching an event by class.
     *
     * @param clazz the class to compare the event to
     * @param <T>   the type of event the finished matcher will handle
     * @return the rule
     */
    public static <T> ClassRule<T> when(final Class<T> clazz) {
        return new ClassRule<>(clazz);
    }

    /**
     * Execute the switcher against the matchers provided.
     *
     * @param matchers the matchers to match the event against.
     * @return the original event being matched
     */
    public Object with(final EventMatcher... matchers) {
        for (EventMatcher matcher : matchers) {
            if (matcher.matches(event)) {
                return matcher.apply(event);
            }
        }

        throw new IllegalArgumentException(format("Could not find a rule to match %s", event));
    }
}
