package uk.gov.justice.services.common.util;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;

import uk.gov.justice.services.common.converter.ZonedDateTimes;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import javax.json.JsonString;

/**
 * Utility class for date creation to allow mocking of dates in tests.
 *
 * Deprecated - inject the {@link Clock} interface instead.
 */
@Deprecated
public class DateTimeProvider {

    /**
     * @return the current UTC datetime
     */
    public ZonedDateTime now() {
        return of(LocalDateTime.now(), UTC);
    }

    /**
     * Parse a {@link JsonString} to create a {@link ZonedDateTime} converted to UTC.
     *
     * @param source the datetime as a string
     * @return the date time converted to UTC
     */
    public ZonedDateTime fromJsonString(final JsonString source) {
        return ZonedDateTimes.fromJsonString(source);
    }

    /**
     * Format a {@link ZonedDateTime} converted to UTC as a string.
     *
     * @param source the date time
     * @return the date time as a string
     */
    public String toString(final ZonedDateTime source) {
        return ZonedDateTimes.toString(source);
    }

    /**
     * Parse a correctly formatted String to create a {@link ZonedDateTime} converted to UTC.
     *
     * @param iso8601DateTimeString the datetime as a correctly formatted string
     * @return the date time converted to UTC
     */
    public ZonedDateTime fromString(final String iso8601DateTimeString) {
        return ZonedDateTimes.fromString(iso8601DateTimeString);
    }

    /**
     * Convert a {@link Timestamp} to a UTC {@link ZonedDateTime}.
     *
     * @param timestamp the timestamp to be covereted
     * @return the date time as a UTC ZoneDateTIme
     */
    public ZonedDateTime fromSqlTimestamp(final Timestamp timestamp) {
        return ZonedDateTimes.fromSqlTimestamp(timestamp);
    }

    /**
     * Convert a UTC {@link ZonedDateTime} to a {@link Timestamp}.
     *
     * @param dateTime the dateTime to be covereted
     * @return the date time as a UTC ZoneDateTIme
     */
    public Timestamp toSqlTimestamp(final ZonedDateTime dateTime) {
        return ZonedDateTimes.toSqlTimestamp(dateTime);
    }
}
