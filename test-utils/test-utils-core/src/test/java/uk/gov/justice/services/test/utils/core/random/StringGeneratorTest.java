package uk.gov.justice.services.test.utils.core.random;

import static org.junit.rules.ExpectedException.none;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StringGeneratorTest {

    private static final int NUMBER_OF_TIMES = 100000;

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldThrowExceptionWhenLengthIsNotGreaterThanZero() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("String length needs to be greater than zero. Got -10");

        new StringGenerator(-10);
    }

    @Test
    public void shouldGenerateRandomStringOfGivenLength() throws Exception {
        final Integer length = 100;
        final StringGenerator stringGenerator = new StringGenerator(length);

        typeCheck(stringGenerator, s -> s.length() == length && s.matches("[0-9A-Za-z]{100}")).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateStringOfLength10ByDefault() throws Exception {
        final StringGenerator stringGenerator = new StringGenerator();

        typeCheck(stringGenerator, s -> s.length() == 10 && s.matches("[0-9A-Za-z]{10}")).verify(times(NUMBER_OF_TIMES));
    }
}