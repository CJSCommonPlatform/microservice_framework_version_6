package uk.gov.justice.services.test.utils.core.random;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.EnumSet.allOf;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class RandomGeneratorTest {

    private static final int NUMBER_OF_TIMES = 10;

    @Test
    public void shouldGenerateRandomBigDecimal() {
        // given
        final Generator<BigDecimal> bigDecimalGenerator = RandomGenerator.BIG_DECIMAL;

        // when
        typeCheck(bigDecimalGenerator, s -> bigDecimalGenerator.next().compareTo(bigDecimalGenerator.next()) != 0).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomDouble() {
        // given
        final Generator<Double> doubleGenerator = RandomGenerator.DOUBLE;

        // when
        typeCheck(doubleGenerator, s -> !Objects.equals(doubleGenerator.next(), doubleGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomEmailAddress() {
        // given
        final Generator<String> emailAddressGenerator = RandomGenerator.EMAIL_ADDRESS;

        // when
        typeCheck(emailAddressGenerator, s -> !emailAddressGenerator.next().equals(emailAddressGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomInteger() {
        // given
        final Generator<Integer> integerGenerator = RandomGenerator.INTEGER;

        // when
        typeCheck(integerGenerator, s -> !Objects.equals(integerGenerator.next(), integerGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomString() {
        // given
        final Generator<String> stringGenerator = RandomGenerator.STRING;

        // when
        typeCheck(stringGenerator,
                s -> !stringGenerator.next().equals(stringGenerator.next()))
                .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomLong() {
        // given
        final Generator<Long> longGenerator = RandomGenerator.LONG;

        // when
        typeCheck(longGenerator, s -> !Objects.equals(longGenerator.next(), longGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomPercentage() {
        // given
        final Generator<BigDecimal> percentageGenerator = RandomGenerator.PERCENTAGE;

        // when
        typeCheck(percentageGenerator, s -> percentageGenerator.next().compareTo(percentageGenerator.next()) != 0).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomNiNumber() {
        // given
        final Generator<String> niNumberGenerator = RandomGenerator.NI_NUMBER;

        // when
        typeCheck(niNumberGenerator, s -> !niNumberGenerator.next().equals(niNumberGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomPostCode() {
        // given
        final Generator<String> postCodeGenerator = RandomGenerator.POST_CODE;

        // when
        typeCheck(postCodeGenerator, s -> !postCodeGenerator.next().equals(postCodeGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomUri() {
        // given
        final Generator<URI> uriGenerator = RandomGenerator.URI;

        // when
        typeCheck(uriGenerator, s -> !uriGenerator.next().equals(uriGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomUuid() {
        // given
        final Generator<UUID> uuidGenerator = RandomGenerator.UUID;

        // when
        typeCheck(uuidGenerator, s -> !uuidGenerator.next().equals(uuidGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomForwardDate() {
        // given
        final Generator<LocalDate> futureLocalDateGenerator = RandomGenerator.FUTURE_LOCAL_DATE;

        // when
        typeCheck(futureLocalDateGenerator, s -> !futureLocalDateGenerator.next().isEqual(futureLocalDateGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomBackwardDate() {
        // given
        final Generator<LocalDate> pastLocalDateGenerator = RandomGenerator.PAST_LOCAL_DATE;

        // when
        typeCheck(pastLocalDateGenerator, s -> !pastLocalDateGenerator.next().isEqual(pastLocalDateGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateStringOfLength() {
        final Generator<String> stringOfLength5Generator = RandomGenerator.string(5);
        typeCheck(stringOfLength5Generator, s -> (!(stringOfLength5Generator.next().length() < 5))).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateValuesFromIterable() {
        // given
        final List<Integer> integers = newArrayList(1, 2, 3, 4, 5);
        // and
        final Generator<Integer> valuesGenerator = RandomGenerator.values(integers);

        // when
        typeCheck(valuesGenerator, s -> ((integers.contains(valuesGenerator.next())))).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateIntegerEqualToOrLessThanMax() {
        // given
        final Integer max = 100;
        // and
        final Generator<Integer> integerWithMaxGenerator = RandomGenerator.integer(max);

        // when
        typeCheck(integerWithMaxGenerator, s -> ((integerWithMaxGenerator.next() <= max))).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateBigDecimalEqualToOrLessThanMax() {
        // given
        final Integer max = 100;
        // and
        final Generator<BigDecimal> bigDecimalWithMaxGenerator = RandomGenerator.bigDecimal(max);

        // when
        typeCheck(bigDecimalWithMaxGenerator,
                s -> ((bigDecimalWithMaxGenerator.next().compareTo(new BigDecimal(max)) != 1))).verify(times(NUMBER_OF_TIMES));
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
        typeCheck(bigDecimalWithMaxAndDecimalGenerator,
                s -> ((bigDecimalWithMaxAndDecimalGenerator.next().compareTo(new BigDecimal(max + "." + decimalPlaces)) != 1)))
                .verify(times(NUMBER_OF_TIMES));
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
        typeCheck(doubleWithMaxAndDecimalGenerator,
                s -> ((doubleWithMaxAndDecimalGenerator.next().compareTo(Double.parseDouble(max + "." + decimalPlaces)) != 1)))
                .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldPickAnEnumFromAvailableElements() {
        // given
        final Generator<TimeUnit> enumGenerator = randomEnum(TimeUnit.class);

        // then
        typeCheck(enumGenerator, s -> allOf(TimeUnit.class).contains(enumGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldAlwaysPickSameElementFromAnEnumWithSingleElement() {
        // given
        final Generator<SingleEnum> enumGenerator = randomEnum(SingleEnum.class);

        // then
        typeCheck(enumGenerator, s -> enumGenerator.next().compareTo(enumGenerator.next()) == 0).verify(times(NUMBER_OF_TIMES));
    }

    enum SingleEnum {SINGLE_VALUE}
}