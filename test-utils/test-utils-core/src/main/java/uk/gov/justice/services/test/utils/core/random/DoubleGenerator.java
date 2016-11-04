package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;
import static java.math.BigDecimal.ROUND_HALF_EVEN;

import java.math.BigDecimal;

class DoubleGenerator extends Generator<Double> {

    private static final double DEFAULT_MIN = -Double.MAX_VALUE;
    private static final double DEFAULT_MAX = Double.MAX_VALUE;
    private static final int DEFAULT_SCALE = 2;

    private final Double min;
    private final Double max;
    private final Integer maxScale;

    public DoubleGenerator(final Double min, final Double max, final Integer maxScale) {
        validateRangeAndScale(min, max, maxScale);

        this.min = min;
        this.max = max;
        this.maxScale = maxScale;
    }

    public DoubleGenerator() {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_SCALE);
    }

    public DoubleGenerator(final Long min, final Long max, final Integer maxScale) {
        this(new Double(min), new Double(max), maxScale);
    }

    @Override
    public Double next() {
        final double randomDouble;
        if (Double.isInfinite(max - min)) {
            if (RANDOM.nextDouble() < 0.5) {
                randomDouble = min * RANDOM.nextDouble();
            } else {
                randomDouble = max * RANDOM.nextDouble();
            }
        } else {
            randomDouble = min + (max - min) * RANDOM.nextDouble();
        }
        return new BigDecimal(randomDouble).setScale(maxScale, ROUND_HALF_EVEN).doubleValue();
    }

    private void validateRangeAndScale(final Double min, final Double max, final Integer scale) {
        if (min.compareTo(max) >= 0) {
            throw new IllegalArgumentException(format("Min value cannot be greater than or equal to Max value, got Min: %s and Max: %s", min, max));
        }
        if (scale < 0) {
            throw new IllegalArgumentException(format("Scale cannot be less than zero, got %s", scale));
        }
    }

}
