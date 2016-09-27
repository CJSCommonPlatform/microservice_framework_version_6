package uk.gov.justice.services.test.utils.core.random;

public class BooleanGenerator implements Generator<Boolean> {
    public Boolean next() {
        return RANDOM.nextBoolean();
    }
}
