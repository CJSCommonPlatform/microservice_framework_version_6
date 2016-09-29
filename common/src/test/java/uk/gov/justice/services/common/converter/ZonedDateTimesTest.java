package uk.gov.justice.services.common.converter;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.time.ZonedDateTime.parse;
import static javax.json.Json.createObjectBuilder;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        final ZonedDateTime dateTime = ZonedDateTimes.fromString("2016-01-21T23:42:03.522+07:00");
        assertThat(dateTime.getZone(), equalTo(ZoneId.of("UTC").normalized()));
    }

    @Test
    public void shouldConvertJsonStringToUtc() {
        final ZonedDateTime dateTime = ZonedDateTimes.fromJsonString(createJsonString("2016-01-21T23:42:03.522+07:00"));
        assertThat(dateTime.getZone(), equalTo(ZoneId.of("UTC").normalized()));
    }

    @Test
    public void shouldKeepCorrectTimeWhenConvertingStringToUtc() {
        final ZonedDateTime dateTime = ZonedDateTimes.fromJsonString(createJsonString("2016-01-21T23:42:03.522+07:00"));
        assertThat(dateTime.toInstant(), equalTo(parse("2016-01-21T16:42:03.522Z").toInstant()));
    }

    @Test
    public void shouldConvertNonUtcToUtcString() {
        final String dateTime = ZonedDateTimes.toString(parse("2016-01-21T23:42:03.522+07:00"));
        assertThat(dateTime, equalTo("2016-01-21T16:42:03.522Z"));
    }

    @Test
    public void shouldConvertNonUtcToUtcString2() {
        final String dateTime = ZonedDateTimes.toString(parse("2016-07-25T23:09:01.0+05:00"));
        assertThat(dateTime, equalTo("2016-07-25T18:09:01.000Z"));
    }

    @Test
    public void shouldConvertZoneDateTimeToSqlTimestamp() {
        final ZonedDateTime zonedDateTime = of(LocalDateTime.now(), UTC);
        final Timestamp dateTime = ZonedDateTimes.toSqlTimestamp(zonedDateTime);

        assertThat(dateTime.toInstant().getEpochSecond(), equalTo(zonedDateTime.toInstant().getEpochSecond()));
    }

    @Test
    public void shouldConvertSqlTimestampToZoneDateTime() {
        final Timestamp timestamp = Timestamp.valueOf("2016-07-25 23:09:00.123");
        final ZonedDateTime dateTime = ZonedDateTimes.fromSqlTimestamp(timestamp);

        assertThat(dateTime.toInstant().getEpochSecond(), equalTo(timestamp.toInstant().getEpochSecond()));
    }

    private JsonString createJsonString(final String source) {
        return createObjectBuilder().add("tmp", source).build().getJsonString("tmp");
    }
}
