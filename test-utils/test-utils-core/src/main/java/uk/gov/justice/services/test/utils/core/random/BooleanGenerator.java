package uk.gov.justice.services.test.utils.core.random;

public class BooleanGenerator extends Generator<Boolean> {
    @Override
    public Boolean next() {
        return RANDOM.nextBoolean();
    }
}
