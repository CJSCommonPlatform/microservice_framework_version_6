package uk.gov.justice.services.common.util;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * Simple wrapper around date creation to allow mocking of dates in tests
 */
public class DateTimeProvider {

    /**
     * @return the current UTC datetime
     */
    public ZonedDateTime now() {
        return of(LocalDateTime.now(), UTC);
    }
}
