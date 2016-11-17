package uk.gov.justice.services.test.utils.core.random;

import static com.btmatthews.hamcrest.regex.PatternMatcher.matches;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isAlpha;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public class OutCodeGeneratorTest {

    @Test
    public void shouldGenerateOutwardCodeParts() {
        final OutCodeGenerator outCodeGenerator = new OutCodeGenerator();

        for (int i = 0; i < 10000; i++) {
            final String outwardCode = outCodeGenerator.next();
            final String errorMessage = format("generated outward code %s is invalid", outwardCode);

            assertThat(outwardCode.length(), is(greaterThanOrEqualTo(2)));
            assertThat(outwardCode.length(), is(lessThanOrEqualTo(4)));

            assertThat(errorMessage, outwardCode, matches("[A-Z0-9]+"));

            assertThat(errorMessage, outwardCode.substring(0, 1), matches("(?=[A-Z])([^QVX])"));
            if (outwardCode.length() >= 2 && isAlpha(outwardCode.substring(1, 2))) {
                assertThat(errorMessage, outwardCode.substring(1, 2), matches("(?=[A-Z])([^IJZ])"));
            }
            if (outwardCode.length() >= 3 && isAlpha(outwardCode.substring(2, 3))) {
                assertThat(errorMessage, outwardCode.substring(2, 3), matches("[ABCDEFGHJKPSTUW]"));
            }
        }
    }
}
