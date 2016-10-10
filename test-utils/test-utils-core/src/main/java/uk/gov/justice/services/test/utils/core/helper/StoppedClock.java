package uk.gov.justice.services.test.utils.core.helper;

import uk.gov.justice.services.common.util.Clock;

import java.time.ZonedDateTime;

/**
 * A stopped clock implementation for testing - for when you want all calls to the clock to return
 * the same time.
 */
public class StoppedClock implements Clock {

    private final ZonedDateTime zonedDateTime;

    public StoppedClock(final ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    @Override
    public ZonedDateTime now() {
        return zonedDateTime;
    }
}
