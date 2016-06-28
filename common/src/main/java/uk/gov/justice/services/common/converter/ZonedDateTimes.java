package uk.gov.justice.services.common.converter;

import static java.time.ZoneOffset.UTC;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.json.JsonString;

/**
 * Utility functions for converting to and from date time objects.
 */
public final class ZonedDateTimes {

    public static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Private constructor to avoid misuse of utility class.
     */
    private ZonedDateTimes() {
    }

    /**
     * Parse a {@link JsonString} to create a {@link ZonedDateTime} converted to UTC.
     *
     * @param source the datetime as a string
     * @return the date time converted to UTC
     */
    public static ZonedDateTime fromJsonString(final JsonString source) {
        return ZonedDateTime.parse(source.getString()).withZoneSameInstant(UTC);
    }

    /**
     * Format a {@link ZonedDateTime} converted to UTC as a string.
     *
     * @param source the date time
     * @return the date time as a string
     */
    public static String toString(final ZonedDateTime source) {
        return source.withZoneSameInstant(UTC).format(DateTimeFormatter.ofPattern(ISO_8601));
    }
}
