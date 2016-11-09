package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;

public class IntegerGenerator extends Generator<Integer> {

    /**
     * The minimum value of the range
     */
    private final int min;

    /**
     * The maximum value of the range
     */
    private final int max;

    /**
     * Package Access only
     * 
     * @see RandomGenerator
     */
    IntegerGenerator() {
        this.min = Integer.MIN_VALUE;
        this.max = Integer.MAX_VALUE;
    }

    /**
     * Package Access only
     * 
     * @param min value included
     * @param max value excluded
     * @see RandomGenerator
     */
    IntegerGenerator(final int min, final int max) {
        if (min >= max) {
            throw new IllegalArgumentException(
                            format("Min value cannot be greater than or equal to Max value, got Min: %s and Max: %s", min, max));
        }
        this.min = min;
        this.max = max;
    }

    @Override
    public Integer next() {
        return RANDOM.ints(min, max).limit(1).findFirst().getAsInt();
    }
}
