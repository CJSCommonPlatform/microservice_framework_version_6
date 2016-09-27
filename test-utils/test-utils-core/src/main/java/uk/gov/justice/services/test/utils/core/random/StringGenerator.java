package uk.gov.justice.services.test.utils.core.random;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class StringGenerator implements Generator<String> {
    private Integer length = 3;

    public StringGenerator(Integer length) {
        if (length != null) {
            this.length = length;
        }
    }

    @Override
    public String next() {
        return randomAlphanumeric(length);
    }
}
