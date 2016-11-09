package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class StringGenerator extends Generator<String> {

    private static final int DEFAULT_MAX = 10;
    private final int length;

    public StringGenerator() {
        this(DEFAULT_MAX);
    }

    public StringGenerator(final int length) {
        if (length <= 0) {
            throw new IllegalArgumentException(format("String length needs to be greater than zero. Got %s", length));
        }
        this.length = length;
    }

    @Override
    public String next() {
        return randomAlphanumeric(length);
    }
}
