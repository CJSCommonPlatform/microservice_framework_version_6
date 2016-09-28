package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.random;

public class PostcodeGenerator extends Generator<String> {
    @Override
    public String next() {
        return format("%s%s%01d %01d%s",
                random(1, "ABCDEFGHIJKLMNOPRSTUWYZ"),
                random(1, "ABCDEFGHKLMNOPQRSTUVWXY"),
                RandomGenerator.integer(9).next(),
                RandomGenerator.integer(9).next(),
                random(2, "ABDEFGHJLNPQRSTUWXYZ"));
    }
}
