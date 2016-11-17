package uk.gov.justice.services.test.utils.core.random;

import static com.btmatthews.hamcrest.regex.PatternMatcher.matches;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public class InCodeGeneratorTest {

    @Test
    public void shouldGenerateInwardCodeParts() {
        final InCodeGenerator inCodeGenerator = new InCodeGenerator();

        for (int i = 0; i < 10000; i++) {
            final String inwardCode = inCodeGenerator.next();
            final String errorMessage = format("generated inward code %s is invalid", inwardCode);

            assertThat(inwardCode.length(), is(3));
            assertThat(errorMessage, inwardCode, matches("[0-9]((?=[A-Z])([^CIKMOV])){2}"));
        }
    }

}