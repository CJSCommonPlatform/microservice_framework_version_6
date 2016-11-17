package uk.gov.justice.services.test.utils.core.random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.string;

import org.junit.Test;

public class LocalPartValidatorTest {

    private final LocalPartValidator localPartValidator = new LocalPartValidator();

    @Test
    public void shouldPassValidationWhenLocalPartIsValid() {
        assertThat(localPartValidator.validate("halloween.party"), is(true));
    }

    @Test
    public void shouldFailValidationWhenLocalPartStartsWithADot() {
        assertThat(localPartValidator.validate(".halloween"), is(false));
    }

    @Test
    public void shouldFailValidationWhenLocalPartEndsWithADot() {
        assertThat(localPartValidator.validate("halloween."), is(false));
    }

    @Test
    public void shouldFailValidationWhenLocalPartIsNullOrEmpty() {
        assertThat(localPartValidator.validate(null), is(false));
        assertThat(localPartValidator.validate(""), is(false));
    }

    @Test
    public void shouldFailValidationWhenLocalPartIsGreaterThan64Characters() {
        assertThat(localPartValidator.validate(string(65).next()), is(false));
    }

    @Test
    public void shouldFailValidationWhenLocalPartHasConsecutiveDots() {
        assertThat(localPartValidator.validate("halloween..party"), is(false));
    }

    @Test
    public void shouldPassValidationWhenLocalPartHasConsecutiveDotsAndQuoted() {
        assertThat(localPartValidator.validate("\"halloween..party\""), is(true));
    }

    @Test
    public void shouldFailValidationWhenLocalPartHasNonStandardCharacterAndNotQuoted() {
        assertThat(localPartValidator.validate("halloween party"), is(false));
        assertThat(localPartValidator.validate("halloween\"party"), is(false));
        assertThat(localPartValidator.validate("halloween(party"), is(false));
        assertThat(localPartValidator.validate("halloween)party"), is(false));
        assertThat(localPartValidator.validate("halloween,party"), is(false));
        assertThat(localPartValidator.validate("halloween:party"), is(false));
        assertThat(localPartValidator.validate("halloween;party"), is(false));
        assertThat(localPartValidator.validate("halloween<party"), is(false));
        assertThat(localPartValidator.validate("halloween>party"), is(false));
        assertThat(localPartValidator.validate("halloween@party"), is(false));
        assertThat(localPartValidator.validate("halloween[party"), is(false));
        assertThat(localPartValidator.validate("halloween]party"), is(false));
        assertThat(localPartValidator.validate("halloween\\party"), is(false));
    }

    @Test
    public void shouldFailValidationWhenLocalPartHasQuoteOrBackslashAndNotPrecededWithBackslash() {
        assertThat(localPartValidator.validate("\"halloween\"party\""), is(false));
        assertThat(localPartValidator.validate("\"halloween\\party\""), is(false));
    }

    @Test
    public void shouldPassValidationWhenLocalPartHasQuoteOrBackslashAndPrecededWithBackslash() {
        assertThat(localPartValidator.validate("\"halloween\\\"party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween\\\\party\""), is(true));
    }

    @Test
    public void shouldPassValidationWhenLocalPartHasNonStandardCharacterAndQuoted() {
        assertThat(localPartValidator.validate("\"halloween party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween(party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween)party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween,party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween:party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween;party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween<party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween>party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween@party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween[party\""), is(true));
        assertThat(localPartValidator.validate("\"halloween]party\""), is(true));
    }
}