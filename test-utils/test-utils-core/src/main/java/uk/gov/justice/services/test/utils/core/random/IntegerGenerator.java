package uk.gov.justice.services.test.utils.core.random;

class IntegerGenerator extends Generator<Integer> {

    private final Integer max;

    public IntegerGenerator(final Integer max) {
        this.max = max;
    }

    @Override
    public Integer next() {
        return RANDOM.nextInt(max);
    }
}
