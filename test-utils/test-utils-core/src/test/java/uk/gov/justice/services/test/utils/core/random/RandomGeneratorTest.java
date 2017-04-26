package uk.gov.justice.services.test.utils.core.random;

import static com.btmatthews.hamcrest.regex.PatternMatcher.matches;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.math.BigDecimal.ROUND_HALF_EVEN;
import static java.time.LocalDate.now;
import static java.util.EnumSet.allOf;
import static org.apache.commons.lang3.StringUtils.isAlpha;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.string;

import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.test.utils.core.helper.StoppedClock;

import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomGeneratorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomGeneratorTest.class);

    private static final int NUMBER_OF_TIMES = 10000;
    private static final String BIG_DECIMAL_PATTERN = "(-)?(0|(?!0)\\d{1,10})\\.\\d{2}";
    private static final String PERCENTAGE_PATTERN = "((0|(?!0)\\d{1,2})\\.\\d{2})|100.00";
    private static final String DOUBLE_WITH_OPTIONAL_FRACTION_PATTERN = "(-)?(0|(?!0)\\d{1,309})(\\.\\d{1,2})?";
    private static final String NI_NUMBER_PATTERN = "(?!BG)(?!GB)(?!NK)(?!KN)(?!TN)(?!NT)(?!ZZ)(?:[A-CEGHJ-PR-TW-Z][A-CEGHJ-NPR-TW-Z])(?:\\s*\\d\\s*){6}([A-D]|\\s)";
    private static final String LONG_PATTERN = "(-)?(0|(?!0)\\d{1,19})";
    private static final String URI_PATTERN = "(\\(comment\\))?[-0-9a-zA-Z]{1,63}(\\(comment\\))?\\.[-.0-9a-zA-Z]+";

    private final Clock clock = new StoppedClock(new UtcClock().now());

    @Test
    public void shouldGenerateRandomBigDecimal() {
        final Generator<BigDecimal> bigDecimalGenerator = RandomGenerator.BIG_DECIMAL;

        typeCheck(bigDecimalGenerator, s -> s.toPlainString().matches(BIG_DECIMAL_PATTERN)).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomBoolean() {
        final Generator<Boolean> booleanGenerator = RandomGenerator.BOOLEAN;

        typeCheck(booleanGenerator, s -> of(TRUE, FALSE).contains(s)).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomDouble() {
        final Generator<Double> doubleGenerator = RandomGenerator.DOUBLE;

        typeCheck(doubleGenerator, s -> new DecimalFormat("#.###").format(s).matches(DOUBLE_WITH_OPTIONAL_FRACTION_PATTERN)).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomEmailAddress() {
        final Generator<String> emailAddressGenerator = RandomGenerator.EMAIL_ADDRESS;

        typeCheck(emailAddressGenerator, s -> !s.equals(emailAddressGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomInteger() {
        final Generator<Integer> integerGenerator = RandomGenerator.INTEGER;

        typeCheck(integerGenerator, s -> s >= 0 && s < Integer.MAX_VALUE).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomString() {
        final Generator<String> stringGenerator = RandomGenerator.STRING;

        typeCheck(stringGenerator, s -> s.matches("[0-9A-Za-z]{10}")).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomStringOfGivenLength() {
        final Integer length = 100;
        final Generator<String> stringGenerator = string(length);

        typeCheck(stringGenerator, s -> s.matches("[0-9A-Za-z]{100}")).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomLong() {
        final Generator<Long> longGenerator = RandomGenerator.LONG;

        typeCheck(longGenerator, s -> s.toString().matches(LONG_PATTERN)).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomPercentage() {
        final Generator<BigDecimal> percentageGenerator = RandomGenerator.PERCENTAGE;

        typeCheck(percentageGenerator, s -> s.toPlainString().matches(PERCENTAGE_PATTERN)).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomNiNumber() {
        final Generator<String> niNumberGenerator = RandomGenerator.NI_NUMBER;

        typeCheck(niNumberGenerator, s -> s.matches(NI_NUMBER_PATTERN)).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomPostcode() {
        final Generator<String> postcodeGenerator = RandomGenerator.POST_CODE;

        for (int i = 0; i < NUMBER_OF_TIMES; i++) {
            final String postcode = postcodeGenerator.next();
            assertThat(postcode, containsString(" "));
            assertThat(postcode.length(), is(greaterThanOrEqualTo(4)));
            assertThat(postcode.length(), is(lessThanOrEqualTo(8)));

            final String errorMessage = format("generated postcode %s is invalid", postcode);

            final String tokens[] = postcode.split(" ");
            final String outwardCode = tokens[0];

            assertThat(errorMessage, outwardCode, matches("[A-Z0-9]+"));

            assertThat(errorMessage, outwardCode.substring(0, 1), matches("[^QVX]"));
            if (outwardCode.length() >= 2 && isAlpha(outwardCode.substring(1, 2))) {
                assertThat(errorMessage, outwardCode.substring(1, 2), matches("[^IJZ]"));
            }
            if (outwardCode.length() >= 3 && isAlpha(outwardCode.substring(2, 3))) {
                assertThat(errorMessage, outwardCode.substring(2, 3), matches("[ABCDEFGHJKPSTUW]"));
            }

            final String inwardCode = tokens[1];
            assertThat(errorMessage, inwardCode, matches("[0-9][^CIKMOV]{2}"));
        }
    }

    @Test
    public void shouldGenerateRandomUri() {
        final Generator<URI> uriGenerator = RandomGenerator.URI;

        typeCheck(uriGenerator, s -> s.getScheme().equals("http") && s.getAuthority().matches(URI_PATTERN)).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomUuid() {
        final Generator<UUID> uuidGenerator = RandomGenerator.UUID;

        typeCheck(uuidGenerator, s -> !s.equals(uuidGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomFutureLocalDate() {
        final LocalDateTime startDate = now().atStartOfDay();
        final LocalDateTime endDate = now().plus(Period.ofYears(5)).atStartOfDay();
        final Generator<LocalDate> futureLocalDateGenerator = RandomGenerator.FUTURE_LOCAL_DATE;

        LOGGER.debug("Start date: {} End date: {}", startDate, endDate);

        typeCheck(futureLocalDateGenerator, s -> !(s.isBefore(startDate.toLocalDate()) || s.isAfter(endDate.toLocalDate())))
                .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomPastLocalDate() {
        final LocalDateTime startDate = now().atStartOfDay();
        final LocalDateTime endDate = now().minus(Period.ofYears(5)).atStartOfDay();
        final Generator<LocalDate> pastLocalDateGenerator = RandomGenerator.PAST_LOCAL_DATE;

        LOGGER.debug("Start date: {} End date: {}", startDate, endDate);

        typeCheck(pastLocalDateGenerator, s -> !(s.isBefore(endDate.toLocalDate()) || s.isAfter(startDate.toLocalDate())))
                .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomFutureZonedDateTime() {
        final ZonedDateTime startDateTime = clock.now();
        final ZonedDateTime endDateTime = startDateTime.plus(Period.ofYears(5));
        final Set<ZoneId> randomZones = newHashSet();
        final Generator<ZonedDateTime> futureZonedDateTimeGenerator = RandomGenerator.FUTURE_ZONED_DATE_TIME;

        LOGGER.debug("Start dateTime: {} End dateTime: {}", startDateTime, endDateTime);

        typeCheck(futureZonedDateTimeGenerator, s -> {
            randomZones.add(s.getZone());
            return !(s.isBefore(startDateTime.withZoneSameInstant(s.getZone())) || s.isAfter(endDateTime.withZoneSameInstant(s.getZone())));
        }).verify(times(NUMBER_OF_TIMES));

        assertThat(randomZones, hasSize(greaterThan(10)));
    }

    @Test
    public void shouldGenerateRandomPastZonedDateTime() {
        final ZonedDateTime startDateTime = clock.now();
        final ZonedDateTime endDateTime = startDateTime.minus(Period.ofYears(5));
        final Set<ZoneId> randomZones = newHashSet();
        final Generator<ZonedDateTime> pastZonedDateTimeGenerator = RandomGenerator.PAST_ZONED_DATE_TIME;

        LOGGER.debug("Start dateTime: {} End dateTime: {}", startDateTime, endDateTime);

        typeCheck(pastZonedDateTimeGenerator, s -> {
            randomZones.add(s.getZone());
            return !(s.isBefore(endDateTime.withZoneSameInstant(s.getZone())) || s.isAfter(startDateTime.withZoneSameInstant(s.getZone())));
        }).verify(times(NUMBER_OF_TIMES));

        assertThat(randomZones, hasSize(greaterThan(10)));
    }

    @Test
    public void shouldGenerateRandomFutureDateTimeInUTCZone() {
        final ZonedDateTime startDateTime = clock.now();
        final ZonedDateTime endDateTime = startDateTime.plus(Period.ofYears(5));
        final Generator<ZonedDateTime> futureZonedDateTimeGenerator = RandomGenerator.FUTURE_UTC_DATE_TIME;

        LOGGER.debug("Start dateTime: {} End dateTime: {}", startDateTime, endDateTime);

        typeCheck(futureZonedDateTimeGenerator, s -> {
            assertThat(s.getOffset().getId(), is("Z"));
            assertThat(s.getOffset().getTotalSeconds(), is(0));

            return !(s.isBefore(startDateTime) || s.isAfter(endDateTime));
        }).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomPastDateTimeInUTCZone() {
        final ZonedDateTime startDateTime = clock.now();
        final ZonedDateTime endDateTime = startDateTime.minus(Period.ofYears(5));
        final Generator<ZonedDateTime> pastZonedDateTimeGenerator = RandomGenerator.PAST_UTC_DATE_TIME;

        LOGGER.debug("Start dateTime: {} End dateTime: {}", startDateTime, endDateTime);

        typeCheck(pastZonedDateTimeGenerator, s -> {
            assertThat(s.getOffset().getId(), is("Z"));
            assertThat(s.getOffset().getTotalSeconds(), is(0));

            return !(s.isBefore(endDateTime) || s.isAfter(startDateTime));
        }).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateValuesFromIterable() {
        final List<Integer> integers = newArrayList(1, 2, 3, 4, 5);
        final Generator<Integer> valuesGenerator = RandomGenerator.values(integers);

        typeCheck(valuesGenerator, s -> integers.contains(valuesGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateIntegerEqualToOrLessThanMax() {
        final Integer max = 100;
        final Generator<Integer> integerWithMaxGenerator = RandomGenerator.integer(max);

        typeCheck(integerWithMaxGenerator, s -> integerWithMaxGenerator.next() <= max).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateBigDecimalLessThanOrEqualToMax() {
        final Integer max = 100;
        final Generator<BigDecimal> bigDecimalGenerator = RandomGenerator.bigDecimal(max);

        typeCheck(bigDecimalGenerator, s -> s.compareTo(new BigDecimal(max)) != 1).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateBigDecimalLessThanOrEqualToMaxAndDecimalPlaces() {
        final Integer max = 100;
        final Integer scale = 2;
        final Generator<BigDecimal> bigDecimalWithMaxAndDecimalGenerator = RandomGenerator.bigDecimal(max, scale);

        typeCheck(bigDecimalWithMaxAndDecimalGenerator, s -> s.compareTo(new BigDecimal(max).setScale(scale, ROUND_HALF_EVEN)) != 1)
                .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateBigDecimalGreaterThanOrEqualToMinAndLessThanOrEqualToMax() {
        final Integer min = -100;
        final Integer max = 100;
        final Generator<BigDecimal> bigDecimalGenerator = RandomGenerator.bigDecimal(min, max, 0);

        typeCheck(bigDecimalGenerator, s -> s.compareTo(new BigDecimal(min)) != -1 && s.compareTo(new BigDecimal(max)) != 1)
                .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateDoubleGreaterThanOrEqualToMinAndLessThanOrEqualToMaxForBoundsInLong() {
        final Long min = -100L;
        final Long max = 100L;
        final Integer scale = 2;
        final Generator<Double> doubleGenerator = RandomGenerator.doubleValue(min, max, scale);

        typeCheck(doubleGenerator, s -> s >= min && s <= max).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateDoubleGreaterThanOrEqualToMinAndLessThanOrEqualToMaxForBoundsInDouble() {
        final Double min = -100.55;
        final Double max = 100.55;
        final Integer scale = 2;
        final Generator<Double> doubleGenerator = RandomGenerator.doubleValue(min, max, scale);

        typeCheck(doubleGenerator, s -> s >= min && s <= max).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldPickAnEnumFromAvailableElements() {
        final Generator<TimeUnit> enumGenerator = randomEnum(TimeUnit.class);

        typeCheck(enumGenerator, s -> allOf(TimeUnit.class).contains(enumGenerator.next())).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldAlwaysPickSameElementFromAnEnumWithSingleElement() {
        final Generator<SingleEnum> enumGenerator = randomEnum(SingleEnum.class);

        typeCheck(enumGenerator, s -> s.compareTo(enumGenerator.next()) == 0 && s == SingleEnum.SINGLE_VALUE)
                .verify(times(NUMBER_OF_TIMES));
    }

    enum SingleEnum {SINGLE_VALUE}
}