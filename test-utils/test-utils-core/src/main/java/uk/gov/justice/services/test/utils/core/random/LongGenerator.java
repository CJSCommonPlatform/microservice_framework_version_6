package uk.gov.justice.services.test.utils.core.random;

public class LongGenerator extends Generator<Long> {

    @Override
    public Long next() {
        return RANDOM.nextLong();
    }
}
