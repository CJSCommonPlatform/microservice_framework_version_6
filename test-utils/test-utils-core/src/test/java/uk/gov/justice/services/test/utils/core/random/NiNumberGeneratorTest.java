package uk.gov.justice.services.test.utils.core.random;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class NiNumberGeneratorTest {

    private static final int NUMBER_OF_TIMES = 100000;
    private static final String NI_NUMBER_PATTERN = "(?!BG)(?!GB)(?!NK)(?!KN)(?!TN)(?!NT)(?!ZZ)(?:[A-CEGHJ-PR-TW-Z][A-CEGHJ-NPR-TW-Z])(?:\\s*\\d\\s*){6}([A-D]|\\s)";

    @Test
    public void shouldGenerateValidNINumbers() throws Exception {
        // given & when
        final NiNumberGenerator niNumberGenerator = new NiNumberGenerator();

        // then
        typeCheck(niNumberGenerator, s -> s.matches(NI_NUMBER_PATTERN)).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateMultipleUniqueNINumbersWhenRepeatedLargeNumberOfTimes() {
        final Set<String> generatedItems = newHashSet();
        final Set<String> lettersInNINumber = newHashSet();
        final String niNumberPatternWithRegions = "([A-Z]{2})(\\d{6})([A-D])";
        final Pattern niNumberPattern = Pattern.compile(niNumberPatternWithRegions);

        final NiNumberGenerator niNumberGenerator = new NiNumberGenerator();

        typeCheck(niNumberGenerator, niNumber -> {
            generatedItems.add(niNumber);
            final Matcher matcher = niNumberPattern.matcher(niNumber);
            if(matcher.find()) {
                lettersInNINumber.add(format("%s%s", matcher.group(1), matcher.group(3)));
            }
            return niNumber.matches(NI_NUMBER_PATTERN);
        }).verify(times(NUMBER_OF_TIMES));

        assertThat(generatedItems.size(), is(greaterThan(10)));
        assertThat(lettersInNINumber.size(), is(greaterThan(10)));
    }

}