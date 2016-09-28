package uk.gov.justice.services.common.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility functions for converting to and from {@link LocalDate} objects
 * and ISO date format String such as '2011-12-03'
 */
public final class LocalDates {

    private static DateTimeFormatter FORMAT = DateTimeFormatter.ISO_DATE;

    /**
     * Private constructor to avoid misuse of utility class.
     */
    private LocalDates() {
    }

    /**
     * Parses a string in ISO date format such as '2011-12-03' to create a {@link LocalDate} object
     *
     * @param source the @link LocalDate}  as a string in ISO_DATE format
     * @return LocalDate object
     */
    public static LocalDate from(final String source) {
        return LocalDate.parse(source, FORMAT);
    }

    /**
     * Format a {@link LocalDate} object to ISO date format string such as '2011-12-03'
     *
     * @param source the date time
     * @return the date time as a string
     */
    public static String to(final LocalDate source) {
        return source.format(FORMAT);
    }
}
