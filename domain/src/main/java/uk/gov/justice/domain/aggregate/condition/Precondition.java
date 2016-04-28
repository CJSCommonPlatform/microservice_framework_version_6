package uk.gov.justice.domain.aggregate.condition;

import java.util.function.Supplier;

/**
 * Representation of a precondition that can be asserted, and throw an exception if it isn't met.
 */
public class Precondition {

    private final Supplier<Boolean> assertion;

    private Precondition(final Supplier<Boolean> assertion) {
        this.assertion = assertion;
    }

    /**
     * Create a precondition from a boolean value.
     *
     * @param value the value
     * @return the precondition
     */
    public static Precondition assertPrecondition(final boolean value) {
        return new Precondition(() -> value);
    }

    /**
     * Create a precondition from a supplier of a boolean value
     *
     * @param assertion the supplier for the value
     * @return the precondition
     */
    public static Precondition assertPrecondition(final Supplier<Boolean> assertion) {
        return new Precondition(assertion);
    }

    /**
     * Check if the precondition is met (ie, the assertion returns true) and if it is not then throw
     * a runtime exception with the given message.
     *
     * @param message the message
     */
    public void orElseThrow(final String message) {
        orElseThrow(() -> new RuntimeException(message));
    }

    /**
     * Check if the precondition is met (ie, the assertion returns true) and if it is not then throw
     * an exception created by a supplier.
     *
     * @param supplier the exception supplier
     */
    public void orElseThrow(final Supplier<RuntimeException> supplier) {
        if (!assertion.get()) {
            throw supplier.get();
        }
    }
}
