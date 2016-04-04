package uk.gov.justice.services.common.converter;

/**
 * A converter converts a source object of type S to a target of type T.
 */
@FunctionalInterface
public interface Converter<S, T> {

    /**
     * Convert the source object of type S to target type T.
     *
     * @param source the source object to convert, which must be an instance of S (never null)
     * @return the converted object, which must be an instance of T. Never returns null.
     */
    T convert(final S source);
}
