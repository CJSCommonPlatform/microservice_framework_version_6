package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;
import static java.math.BigDecimal.ROUND_HALF_EVEN;

import java.math.BigDecimal;

public class BigDecimalGenerator extends Generator<BigDecimal> {

    private static final long DEFAULT_MIN = -1000000000L;
    private static final long DEFAULT_MAX = 1000000000L;
    private static final int DEFAULT_SCALE = 2;

    private final BigDecimal min;
    private final BigDecimal max;
    private final Integer scale;

    public BigDecimalGenerator(final BigDecimal min, final BigDecimal max, final Integer scale) {
        validateRangeAndScale(min, max, scale);

        this.min = min;
        this.max = max;
        this.scale = scale;
    }

    public BigDecimalGenerator() {
        this(new BigDecimal(DEFAULT_MIN), new BigDecimal(DEFAULT_MAX), DEFAULT_SCALE);
    }

    public BigDecimalGenerator(final Integer min, final Integer max, final Integer scale) {
        this(new BigDecimal(min), new BigDecimal(max), scale);
    }

    @Override
    public BigDecimal next() {
        return generateRandomBigDecimalBetween(min, max).setScale(scale, ROUND_HALF_EVEN);
    }

    private BigDecimal generateRandomBigDecimalBetween(final BigDecimal min, final BigDecimal max) {
        final BigDecimal range = max.subtract(min);
        final BigDecimal randomFactor = new BigDecimal(RANDOM.nextDouble());
        final BigDecimal fraction = range.multiply(randomFactor);

        return min.add(fraction);
    }

    private void validateRangeAndScale(final BigDecimal min, final BigDecimal max, final Integer scale) {
        if (min.compareTo(max) >= 0) {
            throw new IllegalArgumentException(format("Min value cannot be greater than or equal to Max value, got Min: %s and Max: %s", min, max));
        }
        if (scale < 0) {
            throw new IllegalArgumentException(format("Scale cannot be less than zero, got %s", scale));
        }
    }

}
