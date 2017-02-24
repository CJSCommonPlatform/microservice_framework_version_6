package uk.gov.justice.services.test.utils.core.random;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;
import static uk.gov.justice.services.test.utils.core.random.DateGenerator.Direction.FUTURE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.stream.LongStream;

public class LocalDateGenerator extends DateGenerator<LocalDate> {
    private final LocalDate start;
    private final LocalDate end;
    private final Direction direction;

    public LocalDateGenerator(final Period period, final LocalDate start, final Direction direction) {
        this.start = start;
        this.direction = direction;
        this.end = direction == FUTURE ? start.plus(period) : start.minus(period);
    }

    @Override
    public LocalDate next() {
        final ZonedDateTime startZDT = start.atStartOfDay(systemDefault());
        final ZonedDateTime endZDT = end.atStartOfDay(systemDefault());
        final long startMillsecs = startZDT.toInstant().toEpochMilli();
        final long endMillisecs = endZDT.toInstant().toEpochMilli();

        final LongStream longStream;
        final Long randomMillsecs;

        if (direction == FUTURE) {
            longStream = RANDOM.longs(startMillsecs, endMillisecs);
        } else {
            longStream = RANDOM.longs(endMillisecs, startMillsecs);
        }

        randomMillsecs = longStream.limit(1).findFirst().orElse(BOOLEAN.next() ? startMillsecs : endMillisecs);
        return ofEpochMilli(randomMillsecs).atZone(systemDefault()).toLocalDate();
    }

}
