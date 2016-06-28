package uk.gov.justice.services.common.converter;

import static javax.json.Json.createObjectBuilder;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.json.JsonString;

import org.junit.Test;

/**
 * Unit tests for the {@link ZonedDateTimes} utility class.
 */
public class ZonedDateTimesTest {

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(ZonedDateTimes.class);
    }

    @Test
    public void shouldConvertStringToUtc() {
        final ZonedDateTime dateTime = ZonedDateTimes.fromJsonString(createJsonString("2016-01-21T23:42:03.522+07:00"));
        assertThat(dateTime.getZone(), equalTo(ZoneId.of("UTC").normalized()));
    }

    @Test
    public void shouldKeepCorrectTimeWhenConvertingStringToUtc() {
        final ZonedDateTime dateTime = ZonedDateTimes.fromJsonString(createJsonString("2016-01-21T23:42:03.522+07:00"));
        assertThat(dateTime.toInstant(), equalTo(ZonedDateTime.parse("2016-01-21T16:42:03.522Z").toInstant()));
    }

    @Test
    public void shouldConvertNonUtcToUtcString() {
        final String dateTime = ZonedDateTimes.toString(ZonedDateTime.parse("2016-01-21T23:42:03.522+07:00"));
        assertThat(dateTime, equalTo("2016-01-21T16:42:03.522Z"));
    }

    private JsonString createJsonString(final String source) {
        return createObjectBuilder().add("tmp", source).build().getJsonString("tmp");
    }
}
