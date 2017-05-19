package uk.gov.justice.services.test.utils.core.random;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.ZoneOffset.UTC;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.test.utils.core.random.DateGenerator.Direction;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Random generators for common types to support fuzzy testing
 */
public class RandomGenerator {

    public static final Generator<BigDecimal> BIG_DECIMAL = new BigDecimalGenerator();
    public static final Generator<Boolean> BOOLEAN = new BooleanGenerator();
    public static final Generator<Double> DOUBLE = new DoubleGenerator();
    public static final Generator<String> EMAIL_ADDRESS = new EmailAddressGenerator();
    public static final Generator<Integer> INTEGER = new IntegerGenerator(0, Integer.MAX_VALUE);
    public static final Generator<String> STRING = new StringGenerator();
    public static final Generator<Long> LONG = new LongGenerator();
    public static final Generator<BigDecimal> PERCENTAGE = new BigDecimalGenerator(0, 100, 2);
    public static final Generator<String> NI_NUMBER = new NiNumberGenerator();
    public static final Generator<String> POST_CODE = new PostcodeGenerator();
    public static final Generator<URI> URI = new UriGenerator();
    public static final Generator<UUID> UUID = new UUIDGenerator();
    public static final Generator<LocalDate> FUTURE_LOCAL_DATE = new LocalDateGenerator(
            Period.ofYears(5), LocalDate.now(), Direction.FUTURE);
    public static final Generator<LocalDate> PAST_LOCAL_DATE = new LocalDateGenerator(
            Period.ofYears(5), LocalDate.now(), Direction.PAST);
    public static final Generator<ZonedDateTime> FUTURE_ZONED_DATE_TIME = new ZonedDateTimeGenerator(
            Period.ofYears(5), new UtcClock().now(), Direction.FUTURE);
    public static final Generator<ZonedDateTime> PAST_ZONED_DATE_TIME = new ZonedDateTimeGenerator(
            Period.ofYears(5), new UtcClock().now(), Direction.PAST);
    public static final Generator<ZonedDateTime> FUTURE_UTC_DATE_TIME = new ZonedDateTimeGenerator(
            Period.ofYears(5), new UtcClock().now(), Direction.FUTURE, UTC);
    public static final Generator<ZonedDateTime> PAST_UTC_DATE_TIME = new ZonedDateTimeGenerator(
            Period.ofYears(5), new UtcClock().now(), Direction.PAST, UTC);

    public static Generator<String> string(final int length) {
        return new StringGenerator(length);
    }

    public static <T> Generator<T> values(final Iterable<T> values) {
        return new ItemPicker<>(newArrayList(values));
    }

    @SafeVarargs
    public static <T> Generator<T> values(final T... values) {
        return new ItemPicker<>(values);
    }

    public static Generator<Integer> integer(final Integer max) {
        return new IntegerGenerator(0, max);
    }

    public static Generator<Integer> integer(final Integer min, final Integer max) {
        return new IntegerGenerator(min, max);
    }

    public static Generator<BigDecimal> bigDecimal(final Integer min, final Integer max,
                                                   final Integer scale) {
        return new BigDecimalGenerator(min, max, scale);
    }

    /**
     * @param max   the max
     * @param scale the scale
     * @return the BigDecimalGenerator
     * @deprecated Use {@link #bigDecimal(Integer min, Integer max, Integer scale)} instead.
     */
    @Deprecated
    public static Generator<BigDecimal> bigDecimal(final Integer max, final Integer scale) {
        return new BigDecimalGenerator(0, max, scale);
    }

    /**
     * @param max the max
     * @return the BigDecimalGenerator
     * @deprecated Use {@link #bigDecimal(Integer min, Integer max, Integer scale)} instead.
     */
    @Deprecated
    public static Generator<BigDecimal> bigDecimal(final Integer max) {
        return new BigDecimalGenerator(0, max, 2);
    }

    /**
     * @param max           the max
     * @param decimalPlaces the decimal places
     * @return the DoubleGenerator
     * @deprecated Use {@link #doubleValue(Long min, Long max, Integer scale)} instead.
     */
    @Deprecated
    public static Generator<Double> doubleval(final Integer max, final Integer decimalPlaces) {
        return new DoubleGenerator(0L, new Long(max), decimalPlaces);
    }

    public static Generator<Double> doubleValue(final Double min, final Double max,
                                                final Integer scale) {
        return new DoubleGenerator(min, max, scale);
    }

    public static Generator<Double> doubleValue(final Long min, final Long max,
                                                final Integer scale) {
        return new DoubleGenerator(min, max, scale);
    }

    public static <T extends Enum<?>> Generator<T> randomEnum(final Class<T> clazz) {
        return new EnumPicker<>(clazz);
    }
}
