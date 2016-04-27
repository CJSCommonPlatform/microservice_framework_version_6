package uk.gov.justice.domain.aggregate.matcher;

/**
 * An event matcher rule that checks the class of event being matched.
 * @param <T> the type of event the matchers using this rule will be able to consume
 */
public class ClassRule<T> implements Rule<T> {

    private final Class<T> clazz;

    ClassRule(final Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean matches(final Object event) {
        return clazz.isInstance(event);
    }
}
