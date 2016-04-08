package uk.gov.justice.services.common.jpa.converter;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public class LocalDateTimePersistenceConverterTest {

    public static final int HOUR = 11;
    public static final int MINUTE = 59;
    public static final int SECOND = 58;
    private static final int DAY = 25;
    private static final Month MONTH = Month.DECEMBER;
    private static final int YEAR = 2016;

    private LocalDateTimePersistenceConverter localDateTimePersistenceConverter;

    @Before
    public void setup() {
        localDateTimePersistenceConverter = new LocalDateTimePersistenceConverter();
    }

    @Test
    public void shouldReturnValidConvertedDatabaseDate() {
        LocalDateTime dateTime = LocalDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTE, SECOND);
        Timestamp timestamp = localDateTimePersistenceConverter.convertToDatabaseColumn(dateTime);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        assertThat(calendar.get(Calendar.DATE), equalTo(DAY));
        assertThat(calendar.get(Calendar.MONTH), equalTo(MONTH.getValue() - 1));
        assertThat(calendar.get(Calendar.YEAR), equalTo(YEAR));
        assertThat(calendar.get(Calendar.HOUR), equalTo(HOUR));
        assertThat(calendar.get(Calendar.MINUTE), equalTo(MINUTE));
        assertThat(calendar.get(Calendar.SECOND), equalTo(SECOND));
    }

    @Test
    public void shouldReturnNullDatabaseValueWhenGivenNullDate() {
        assertNull(localDateTimePersistenceConverter.convertToDatabaseColumn(null));
    }

    @Test
    public void shouldReturnValidConvertedAttributeDate() {
        Timestamp timestamp = Timestamp.valueOf(String.format("%s-%s-%s %d:%d:%d", YEAR, MONTH.getValue() - 1, DAY, HOUR, MINUTE, SECOND));
        LocalDateTime localDateTime = localDateTimePersistenceConverter.convertToEntityAttribute(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        assertThat(localDateTime.getDayOfMonth(), equalTo(calendar.get(Calendar.DATE)));
        assertThat(localDateTime.getMonth().getValue(), equalTo(calendar.get(Calendar.MONTH) + 1));
        assertThat(localDateTime.getYear(), equalTo(calendar.get(Calendar.YEAR)));
        assertThat(localDateTime.getHour(), equalTo(calendar.get(Calendar.HOUR)));
        assertThat(localDateTime.getMinute(), equalTo(calendar.get(Calendar.MINUTE)));
        assertThat(localDateTime.getSecond(), equalTo(calendar.get(Calendar.SECOND)));
    }

    @Test
    public void shouldReturnEntityAttributeNullWhenGivenNullDate() {
        assertNull(localDateTimePersistenceConverter.convertToEntityAttribute(null));
    }
}