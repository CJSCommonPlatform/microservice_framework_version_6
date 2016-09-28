package uk.gov.justice.services.test.utils.core.random;

import static java.lang.Math.pow;

class DoubleGenerator extends Generator<Double> {

    private final Integer max;
    private final Integer decimalPlaces;

    public DoubleGenerator(final Integer max, final Integer decimalPlaces) {
        this.max = max;
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public Double next() {
        final double pow = pow(10, decimalPlaces);
        return (double) RANDOM.nextInt((int) (max * pow)) / pow;
    }
}
