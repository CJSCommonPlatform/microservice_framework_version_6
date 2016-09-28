package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;

public class EmailAddressGenerator extends Generator<String> {
    @Override
    public String next() {
        return format("%s@%s.%s", RandomGenerator.string(10).next(), RandomGenerator.string(10).next(), RandomGenerator.values("com", "co.uk", "gov.uk", "org", "net").next());
    }
}