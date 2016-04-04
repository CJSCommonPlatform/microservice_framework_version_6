package uk.gov.justice.services.common.converter;

/**
 * A converter converts a source object of type S to a target of type T.
 */
@FunctionalInterface
public interface TypedConverter<S, T> {

    /**
     * Convert the source object of type S to target type T.
     *
     * @param source the source object to convert, which must be an instance of S (never null)
     * @return the converted object, which must be an instance of T. Never returns null.
     * @throws IllegalArgumentException - if the source cannot be converted to the desired target type
     */
    <R extends T> R convert(final S source, final Class<R> clazz);
}
