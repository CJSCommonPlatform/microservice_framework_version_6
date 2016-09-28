package uk.gov.justice.services.common.converter;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static java.lang.String.format;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for the {@link LocalDates} utility class.
 */
public class LocalDatesTest {

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(LocalDates.class);
    }

    @Test
    public void shouldConvertStringToLocalDate() {
        // given
        int year = 2016;
        // and
        int month = 11;
        // and
        int dayOfMonth = 21;
        // and
        String localDateStr = format("%d-%02d-%d", year, month, dayOfMonth);

        // when
        final LocalDate localDate = LocalDates.from(localDateStr);

        // then
        assertThat(localDate, has(year, month, dayOfMonth));

    }

    private Matcher<LocalDate> has(final int year, final int month, final int dayOfMonth) {
        return new TypeSafeDiagnosingMatcher<LocalDate>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("from() should return ").appendValue(format("%d-%d-%d", year, month, dayOfMonth));
            }

            @Override
            protected boolean matchesSafely(LocalDate localDate, Description description) {
                boolean status = true;
                if (localDate.getYear() != year) {
                    description.appendText(format("Year Mismatch, Actual:%d, Expected:%s", localDate.getYear(), year));
                    status = false;
                }
                if (localDate.getMonth().getValue() != month) {
                    description.appendText(format(" Month Mismatch, Actual:%d, Expected:%s", localDate.getMonth().getValue(), month));
                    status = false;
                }
                if (localDate.getDayOfMonth() != dayOfMonth) {
                    description.appendText(format(" Day Of Month Mismatch, Actual:%d, Expected:%s", localDate.getDayOfMonth(), dayOfMonth));
                    status = false;
                }
                return status;
            }
        };
    }

    @Test
    public void shouldConvertLocalDateToString() {
        // given
        LocalDate localDate = LocalDate.now();

        // when
        String str = LocalDates.to(localDate);

        // then
        assertThat(str, isFrom(localDate));
    }

    private Matcher<String> isFrom(final LocalDate localDate) {
        return new TypeSafeDiagnosingMatcher<String>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("to() should return LocalDate:").appendValue(localDate);
            }

            @Override
            protected boolean matchesSafely(String str, Description description) {
                boolean status = true;
                String[] tokens = str.split("-");

                if (tokens.length != 3) {
                    description.appendText("Unformatted string");
                    return false;
                }


                if (Integer.valueOf(tokens[0]) != localDate.getYear()) {
                    description.appendText(format("Year Mismatch, Actual:%s, Expected:%d", tokens[0], localDate.getYear()));
                    status = false;
                }
                if (Integer.valueOf(tokens[1]) != localDate.getMonth().getValue()) {
                    description.appendText(format(" Month Mismatch, Actual:%s, Expected:%02d", tokens[1], localDate.getMonth().getValue()));
                    status = false;
                }
                if (Integer.valueOf(tokens[2]) != localDate.getDayOfMonth()) {
                    description.appendText(format(" Day Of Month Mismatch, Actual:%s, Expected:%d", tokens[2], localDate.getDayOfMonth()));
                    status = false;
                }
                return status;
            }
        };
    }
}
