package uk.gov.justice.services.test.utils.core.random;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneOffset.UTC;
import static uk.gov.justice.services.test.utils.core.random.DateGenerator.Direction.FUTURE;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
        final ZonedDateTime startZDT = start.atStartOfDay(UTC);
        final ZonedDateTime endZDT = end.atStartOfDay(UTC);
        final long startMillsecs = startZDT.toInstant().toEpochMilli();
        final long endMillisecs = endZDT.toInstant().toEpochMilli();

        Long randomMillsecs;

        if (direction == FUTURE) {
            randomMillsecs = RANDOM.longs(startMillsecs, endMillisecs).findFirst().getAsLong();
        } else {
            randomMillsecs = RANDOM.longs(endMillisecs, startMillsecs).findFirst().getAsLong();
        }

        return ofEpochMilli(randomMillsecs).atZone(ZoneId.systemDefault()).toLocalDate();
    }

}
