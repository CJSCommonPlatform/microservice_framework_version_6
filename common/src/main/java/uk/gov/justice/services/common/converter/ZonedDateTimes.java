package uk.gov.justice.services.common.converter;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.parse;
import static java.time.format.DateTimeFormatter.ofPattern;

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
        return fromString(source.getString());
    }

    /**
     * Format a {@link ZonedDateTime} converted to UTC as a string.
     *
     * @param source the date time
     * @return the date time as a string
     */
    public static String toString(final ZonedDateTime source) {
        return source.withZoneSameInstant(UTC).format(ofPattern(ISO_8601));
    }

    /**
     * Parse a correctly formatted String to create a {@link ZonedDateTime} converted to UTC.
     *
     * @param iso8601DateTimeString the datetime as a correctly formatted string
     * @return the date time converted to UTC
     */
    public static ZonedDateTime fromString(final String iso8601DateTimeString) {
        return parse(iso8601DateTimeString).withZoneSameInstant(UTC);
    }
}
