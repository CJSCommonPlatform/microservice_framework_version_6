package uk.gov.justice.services.test.utils.core.random;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class StringGenerator extends Generator<String> {
    private final Integer length;

    public StringGenerator(final Integer length) {
        if (length != null) {
            this.length = length;
        } else {
            this.length = 3;
        }
    }

    @Override
    public String next() {
        return randomAlphanumeric(length);
    }
}
