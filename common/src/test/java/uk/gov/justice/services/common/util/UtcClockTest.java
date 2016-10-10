package uk.gov.justice.services.common.util;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.time.ZonedDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link UtcClock} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class UtcClockTest {

    @InjectMocks
    private UtcClock clock;

    @Test
    public void shouldGetANewZonedDateTimeWithCurrentTime() throws Exception {

        final ZonedDateTime zonedDateTime = clock.now();

        assertThat(zonedDateTime, is(notNullValue()));

        assertThat(zonedDateTime.isAfter(now().minusSeconds(2L)), is(true));
        assertThat(zonedDateTime.getZone(), is(UTC));
    }
}
