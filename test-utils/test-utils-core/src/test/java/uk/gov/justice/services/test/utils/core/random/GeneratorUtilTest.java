package uk.gov.justice.services.test.utils.core.random;

import static com.btmatthews.hamcrest.regex.PatternMatcher.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.checkValidityOfText;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.concat;
import static uk.gov.justice.services.test.utils.core.random.GeneratorUtil.generateStringFromCharacters;

import org.junit.Test;

public class GeneratorUtilTest {

    @Test
    public void shouldGenerateValidStringFromAListOfCharacters() {
        final char validChars[] = "abcef".toCharArray();
        final String actualString = generateStringFromCharacters(validChars, 2, 3);

        assertThat(actualString, matches("[abcef]{2,3}"));
    }

    @Test
    public void shouldConcatenateTwoCharacterArrays() {
        final char firstArray[] = "abcef".toCharArray();
        final char secondArray[] = "hijkl".toCharArray();
        final char resultArray[] = concat(firstArray, secondArray);

        assertThat(new String(resultArray), is("abcefhijkl"));
    }

    @Test
    public void shouldTestThatStringIsComprisedOfValidCharacters() {
        final char actualChars[] = "abc".toCharArray();
        final char validChars[] = "abcef".toCharArray();

        assertThat(checkValidityOfText(new String(actualChars), validChars), is(true));
    }

    @Test
    public void shouldTestThatStringIsComprisedOfInValidCharacters() {
        final char actualChars[] = "abcef".toCharArray();
        final char validChars[] = "bcef".toCharArray();

        assertThat(checkValidityOfText(new String(actualChars), validChars), is(false));
    }

}
