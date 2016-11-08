package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;

public class LongGenerator extends Generator<Long> {

    /**
     * The minimum value of the range
     */
    private final long min;

    /**
     * The maximum value of the range
     */
    private final long max;

    /**
     * Package Access only
     * 
     * @see RandomGenerator
     */
    LongGenerator() {
        this.min = Long.MIN_VALUE;
        this.max = Long.MAX_VALUE;
    }

    /**
     * Package Access only
     * 
     * @param max value excluded
     * @see RandomGenerator
     */
    LongGenerator(final long max) {
        this.min = 0;
        this.max = max;
    }
    /**
     * Package Access only
     * 
     * @param min value included
     * @param max value excluded
     * @see RandomGenerator
     */
    LongGenerator(final long min, final long max) {
        if (min >= max) {
            throw new IllegalArgumentException(
                            format("Min value cannot be greater than or equal to Max value, got Min: %s and Max: %s", min, max));
        }
        this.min = min;
        this.max = max;
    }

    @Override
    public Long next() {
        return RANDOM.longs(min, max).limit(1).findFirst().getAsLong();
    }

}
