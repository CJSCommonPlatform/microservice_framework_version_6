package uk.gov.justice.services.test.utils.core.random;

import java.math.BigDecimal;

import static java.lang.Math.pow;
import static java.math.RoundingMode.FLOOR;

public class BigDecimalGenerator extends Generator<BigDecimal> {

    private final Integer max;
    private final Integer decimalPlaces;

    public BigDecimalGenerator(final Integer max, final Integer decimalPlaces) {
        this.max = max;
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public BigDecimal next() {
        final double pow = pow(10, decimalPlaces);
        return new BigDecimal((long) RANDOM.nextInt((int) (max * pow)) / pow).setScale(decimalPlaces, FLOOR);
    }
}
