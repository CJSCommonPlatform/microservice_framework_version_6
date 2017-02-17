package uk.gov.justice.services.test.utils.core.random;

import static com.google.common.collect.Sets.newHashSet;
import static java.time.Period.ofYears;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.DateGenerator.Direction.PAST;

import uk.gov.justice.services.common.util.UtcClock;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.Test;

public class ZonedDateTimeGeneratorTest {

    private static final int NUMBER_OF_TIMES = 10000;

    @Test
    public void shouldGenerateZonedDateTimeInUTCZone() {
        final ZonedDateTime utcZonedDateTime = new ZonedDateTimeGenerator(ofYears(1), new UtcClock().now(), PAST, UTC).next();

        assertThat(utcZonedDateTime.getOffset().getId(), is("Z"));
        assertThat(utcZonedDateTime.getOffset().getTotalSeconds(), is(0));
    }

    @Test
    public void shouldGenerateDateTimesInDifferentZones() {
        final ZonedDateTimeGenerator randomZonedDateTimeGenerator = new ZonedDateTimeGenerator(ofYears(1), new UtcClock().now(), PAST);
        final Set<ZoneId> randomZones = newHashSet();

        IntStream.range(0, NUMBER_OF_TIMES)
                .forEach(idx -> randomZones.add(randomZonedDateTimeGenerator.next().getZone()));

        assertThat(randomZones, hasSize(greaterThan(10)));
    }

}