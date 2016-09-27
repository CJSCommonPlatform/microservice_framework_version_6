package uk.gov.justice.services.test.utils.core.random;

class LongGenerator implements Generator<Long> {

    @Override
    public Long next() {
        return RANDOM.nextLong();
    }
}
