package uk.gov.justice.services.test.utils.core.random;

import static java.lang.Math.pow;

class DoubleGenerator implements Generator<Double> {

    private final Integer max;
    private final Integer decimalPlaces;

    public DoubleGenerator(Integer max, Integer decimalPlaces) {
        this.max = max;
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public Double next() {
        double pow = pow(10, decimalPlaces);
        return (double) RANDOM.nextInt((int) (max * pow)) / pow;
    }
}
