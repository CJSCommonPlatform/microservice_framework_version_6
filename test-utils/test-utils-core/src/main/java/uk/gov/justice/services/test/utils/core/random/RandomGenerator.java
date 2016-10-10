package uk.gov.justice.services.test.utils.core.random;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

/**
 * Random generators for common types to support fuzzy testing
 */
public class RandomGenerator {

    public static final Generator<BigDecimal> BIG_DECIMAL = new BigDecimalGenerator(999999, 2);
    public static final Generator<Boolean> BOOLEAN = new BooleanGenerator();
    public static final Generator<Double> DOUBLE = new DoubleGenerator(Integer.MAX_VALUE, 2);
    public static final Generator<String> EMAIL_ADDRESS = new EmailAddressGenerator();
    public static final Generator<Integer> INTEGER = new IntegerGenerator(Integer.MAX_VALUE);
    public static final Generator<String> STRING = new StringGenerator();
    public static final Generator<Long> LONG = new LongGenerator();
    public static final Generator<BigDecimal> PERCENTAGE = new BigDecimalGenerator(100, 2);
    public static final Generator<String> NI_NUMBER = new NiNumberGenerator();
    public static final Generator<String> POST_CODE = new PostcodeGenerator();
    public static final Generator<URI> URI = new UriGenerator();
    public static final Generator<UUID> UUID = new UUIDGenerator();
    public static final Generator<LocalDate> FUTURE_LOCAL_DATE = new LocalDateGenerator(Period.ofYears(5), LocalDate.now(), LocalDateGenerator.Direction.FORWARD);
    public static final Generator<LocalDate> PAST_LOCAL_DATE = new LocalDateGenerator(Period.ofYears(5), LocalDate.now(), LocalDateGenerator.Direction.BACKWARD);

    public static Generator<String> string(int length) {
        return new StringGenerator(length);
    }

    public static <T> Generator<T> values(Iterable<T> values) {
        return new ValueGenerator<>(values);
    }

    public static <T> Generator<T> values(T... values) {
        return values(asList(values));
    }

    public static Generator<Integer> integer(Integer max) {
        return new IntegerGenerator(max);
    }

    public static Generator<BigDecimal> bigDecimal(Integer max, Integer decimalPlaces) {
        return new BigDecimalGenerator(max, decimalPlaces);
    }

    public static Generator<BigDecimal> bigDecimal(Integer max) {
        return bigDecimal(max, 2);
    }

    public static Generator<Double> doubleval(Integer max, Integer decimalPlaces) {
        return new DoubleGenerator(max, decimalPlaces);
    }

    public static <T extends Enum<?>> Generator<T> randomEnum(Class<T> clazz) {
        return new EnumPicker<>(clazz);
    }
}
