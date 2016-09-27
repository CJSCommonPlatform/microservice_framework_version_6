package uk.gov.justice.services.test.utils.core.random;

class IntegerGenerator implements Generator<Integer> {

    private Integer max;

    public IntegerGenerator(Integer max) {
        this.max = max;
    }

    @Override
    public Integer next() {
        return RANDOM.nextInt(max);
    }
}
