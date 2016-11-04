package uk.gov.justice.services.test.utils.core.random;

import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DoubleGeneratorTest {

    private static final int NUMBER_OF_TIMES = 100000;

    @Rule
    public ExpectedException expectedException = none();

    @Spy
    private Random random = new Random();

    @Test
    public void shouldGenerateDoubleGreaterThanOrEqualToMinAndLessThanOrEqualToMax() {
        // given
        final Long min = -100L;
        final Long max = 100L;

        // when
        final Generator<Double> doubleGenerator = new DoubleGenerator(min, max, 0);

        // then
        typeCheck(doubleGenerator, d -> d >= min && d <= max).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateDoubleWithOptionalScale() {
        // given
        final Long min = -100L;
        final Long max = 100L;
        final Integer maxScale = 2;

        // when
        final Generator<Double> doubleGenerator = new DoubleGenerator(min, max, maxScale);

        // when
        typeCheck(doubleGenerator, d -> Double.toString(d).matches("(-)?(0|(?!0)\\d{1,3})(\\.\\d{1,2})?")).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldThrowExceptionIfScaleIsNegative() throws Exception {
        // given
        final Long min = -100L;
        final Long max = 100L;
        final Integer scale = -2;
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Scale cannot be less than zero, got -2");

        // when & then
        new DoubleGenerator(min, max, scale).next();
    }

    @Test
    public void shouldThrowExceptionIfMinIsGreaterThanMax() throws Exception {
        // given
        final Long min = 101L;
        final Long max = 100L;
        final Integer scale = 2;
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Min value cannot be greater than or equal to Max value, got Min: 101.0 and Max: 100.0");

        // when & then
        new DoubleGenerator(min, max, scale).next();
    }

    @Test
    public void shouldGenerateDoubleWhenRangeLeadsToInfinite() throws Exception {
        // given
        final Double min = -Double.MAX_VALUE;
        final Double max = Double.MAX_VALUE;
        final Integer scale = 2;
        final int noOfTimes = 1000;

        // when
        final Generator<Double> doubleGenerator = new DoubleGenerator(min, max, scale);
        overrideUnderlyingRandomGenerator(doubleGenerator, random);

        // then
        typeCheck(doubleGenerator, s -> new DecimalFormat("#.###").format(s).matches("(-)?\\d+(\\.\\d{1,2})?")).verify(times(noOfTimes));
        verify(random, Mockito.times(noOfTimes * 2)).nextDouble();
    }

    private <T> void overrideUnderlyingRandomGenerator(final Generator<T> randomGenerator, final Random targetRandom) throws NoSuchFieldException, IllegalAccessException {
        final Field fieldRandom = randomGenerator.getClass().getSuperclass().getDeclaredField("RANDOM");
        fieldRandom.setAccessible(true);
        fieldRandom.set(randomGenerator, targetRandom);
    }

}