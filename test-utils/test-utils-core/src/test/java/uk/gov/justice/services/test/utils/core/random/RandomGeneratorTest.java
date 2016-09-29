package uk.gov.justice.services.test.utils.core.random;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.aTypeCheck;

public class RandomGeneratorTest {

    private static final int NUMBER_OF_TIMES = 10;

    @Test
    public void shouldGenerateRandomBigDecimal() {
        // given
        final Generator<BigDecimal> bigDecimal = RandomGenerator.BIG_DECIMAL;

        // when
        aTypeCheck(bigDecimal,
                s -> bigDecimal.next().compareTo(bigDecimal.next()) != 0)
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateRandomDouble() {
        // given
        final Generator<Double> doubleGenerator = RandomGenerator.DOUBLE;

        // when
        aTypeCheck(doubleGenerator,
                s -> !Objects.equals(doubleGenerator.next(), doubleGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateRandomEmailAddress() {
        // given
        final Generator<String> emailAddressGenerator = RandomGenerator.EMAIL_ADDRESS;

        // when
        aTypeCheck(emailAddressGenerator,
                s -> !emailAddressGenerator.next().equals(emailAddressGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateRandomInteger() {
        // given
        final Generator<Integer> integerGenerator = RandomGenerator.INTEGER;

        // when
        aTypeCheck(integerGenerator,
                s -> !Objects.equals(integerGenerator.next(), integerGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateRandomString() {
        // given
        final Generator<String> stringGenerator = RandomGenerator.STRING;

        // when
        aTypeCheck(stringGenerator,
                s -> !stringGenerator.next().equals(stringGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateRandomLong() {
        // given
        final Generator<Long> longGenerator = RandomGenerator.LONG;

        // when
        aTypeCheck(longGenerator,
                s -> !Objects.equals(longGenerator.next(), longGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES).verify();
    }

    @Test
    public void shouldGenerateRandomPercentage() {
        // given
        final Generator<BigDecimal> percentageGenerator = RandomGenerator.PERCENTAGE;

        // when
        aTypeCheck(percentageGenerator,
                s -> percentageGenerator.next().compareTo(percentageGenerator.next()) != 0)
                .numberOfTimes(NUMBER_OF_TIMES).verify();
    }

    @Test
    public void shouldGenerateRandomNiNumber() {
        // given
        final Generator<String> niNumberGenerator = RandomGenerator.NI_NUMBER;

        // when
        aTypeCheck(niNumberGenerator,
                s -> !niNumberGenerator.next().equals(niNumberGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateRandomPostCode() {
        // given
        final Generator<String> postCodeGenerator = RandomGenerator.POST_CODE;

        // when
        aTypeCheck(postCodeGenerator,
                s -> !postCodeGenerator.next().equals(postCodeGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateRandomUri() {
        // given
        final Generator<URI> uriGenerator = RandomGenerator.URI;

        // when
        aTypeCheck(uriGenerator,
                s -> !uriGenerator.next().equals(uriGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateRandomUuid() {
        // given
        final Generator<UUID> uuidGenerator = RandomGenerator.UUID;

        // when
        aTypeCheck(uuidGenerator,
                s -> !uuidGenerator.next().equals(uuidGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateRandomForwardDate() {
        // given
        final Generator<LocalDate> futureLocalDateGenerator = RandomGenerator.FUTURE_LOCAL_DATE;

        // when
        aTypeCheck(futureLocalDateGenerator,
                s -> !futureLocalDateGenerator.next().isEqual(futureLocalDateGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateRandomBackwardDate() {
        // given
        final Generator<LocalDate> pastLocalDateGenerator = RandomGenerator.PAST_LOCAL_DATE;

        // when
        aTypeCheck(pastLocalDateGenerator,
                s -> !pastLocalDateGenerator.next().isEqual(pastLocalDateGenerator.next()))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateStringOfLength() {
        final Generator<String> stringOfLength5Generator = RandomGenerator.string(5);
        aTypeCheck(stringOfLength5Generator,
                s -> (!(stringOfLength5Generator.next().length() < 5)))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateValuesFromIterable() {
        // given
        final List<Integer> integers = Lists.newArrayList(1, 2, 3, 4, 5);
        // and
        final Generator<Integer> valuesGenerator = RandomGenerator.values(integers);

        // when
        aTypeCheck(valuesGenerator,
                s -> ((integers.contains(valuesGenerator.next()))))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateIntegerEqualToOrLessThanMax() {
        // given
        final Integer max = 100;
        // and
        final Generator<Integer> integerWithMaxGenerator = RandomGenerator.integer(max);

        // when
        aTypeCheck(integerWithMaxGenerator,
                s -> ((integerWithMaxGenerator.next() <= max)))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateBigDecimalEqualToOrLessThanMax() {
        // given
        final Integer max = 100;
        // and
        final Generator<BigDecimal> bigDecimalWithMaxGenerator = RandomGenerator.bigDecimal(max);

        // when
        aTypeCheck(bigDecimalWithMaxGenerator,
                s -> ((bigDecimalWithMaxGenerator.next().compareTo(new BigDecimal(max)) != 1)))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateBigDecimalBelowMaxAndDecimalPlaces() {
        // given
        final Integer max = 100;
        // and
        final Integer decimalPlaces = 2;
        // and
        final Generator<BigDecimal> bigDecimalWithMaxAndDecimalGenerator = RandomGenerator.bigDecimal(max, decimalPlaces);


        // when
        aTypeCheck(bigDecimalWithMaxAndDecimalGenerator,
                s -> ((bigDecimalWithMaxAndDecimalGenerator.next().compareTo(new BigDecimal(max + "." + decimalPlaces)) != 1)))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }

    @Test
    public void shouldGenerateDoubleBelowMaxAndDecimalPlaces() {
        // given
        final Integer max = 100;
        // and
        final Integer decimalPlaces = 2;
        // and
        final Generator<Double> doubleWithMaxAndDecimalGenerator = RandomGenerator.doubleval(max, decimalPlaces);

        // when
        aTypeCheck(doubleWithMaxAndDecimalGenerator,
                s -> ((doubleWithMaxAndDecimalGenerator.next().compareTo(Double.parseDouble(max + "." + decimalPlaces)) != 1)))
                .numberOfTimes(NUMBER_OF_TIMES)
                .verify();
    }
}