package uk.gov.justice.services.test.utils.core.random;

import java.math.BigDecimal;

import static java.lang.Math.pow;
import static java.math.RoundingMode.FLOOR;

public class BigDecimalGenerator implements Generator<BigDecimal> {

    private final Integer max;
    private final Integer decimalPlaces;

    public BigDecimalGenerator(Integer max, Integer decimalPlaces) {
        this.max = max;
        this.decimalPlaces = decimalPlaces;
    }

    public BigDecimal next() {
        double pow = pow(10, decimalPlaces);
        return new BigDecimal((long) RANDOM.nextInt((int) (max * pow)) / pow).setScale(decimalPlaces, FLOOR);
    }
}
