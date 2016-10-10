package uk.gov.justice.services.common.util;

import static java.time.ZoneOffset.UTC;

import java.time.ZonedDateTime;

import javax.enterprise.context.ApplicationScoped;

/**
 * Implementation of a clock that always generates a {@link java.time.ZonedDateTime} in UTC.
 */
@ApplicationScoped
public class UtcClock implements Clock {

    @Override
    public ZonedDateTime now() {
        return ZonedDateTime.now(UTC);
    }
}
