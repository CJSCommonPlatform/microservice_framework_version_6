package uk.gov.justice.services.test.utils.core.random;

import static org.junit.rules.ExpectedException.none;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;

import java.math.BigDecimal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BigDecimalGeneratorTest {

    private static final int NUMBER_OF_TIMES = 100000;

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldGenerateBigDecimalGreaterThanOrEqualToMinAndLessThanOrEqualToMax() {
        // given
        final Integer min = -100;
        final Integer max = 100;

        // when
        final Generator<BigDecimal> bigDecimalGenerator = new BigDecimalGenerator(min, max, 0);

        // then
        typeCheck(bigDecimalGenerator,
                s -> (s.compareTo(new BigDecimal(min)) != -1 && s.compareTo(new BigDecimal(max)) != 1))
                .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateBigDecimalWithSpecifiedScale() {
        // given
        final Integer min = -100;
        final Integer max = 100;
        final Integer scale = 2;

        // when
        final Generator<BigDecimal> bigDecimalGenerator = new BigDecimalGenerator(min, max, scale);

        // when
        typeCheck(bigDecimalGenerator, s -> s.toPlainString().matches("(-)?(0|(?!0)\\d{1,3})\\.\\d{2}"))
                .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldThrowExceptionIfScaleIsNegative() throws Exception {
        // given
        final Integer min = -100;
        final Integer max = 100;
        final Integer scale = -2;
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Scale cannot be less than zero, got -2");

        // when & then
        new BigDecimalGenerator(min, max, scale);
    }

    @Test
    public void shouldThrowExceptionIfMinIsGreaterThanMax() throws Exception {
        // given
        final Integer min = 101;
        final Integer max = 100;
        final Integer scale = 2;
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Min value cannot be greater than or equal to Max value, got Min: 101 and Max: 100");

        // when & then
        new BigDecimalGenerator(min, max, scale);
    }

    @Test
    public void shouldHandleLargeNumbers() {
        // given
        final BigDecimal min = new BigDecimal(-Double.MAX_VALUE);
        final BigDecimal max = new BigDecimal(Double.MAX_VALUE);
        final Integer scale = 2;

        // when
        final Generator<BigDecimal> bigDecimalGenerator = new BigDecimalGenerator(min, max, scale);

        // then
        typeCheck(bigDecimalGenerator, s -> s.toPlainString().matches("(-)?\\d+\\.\\d{2}")).verify(times(1000));
    }
}