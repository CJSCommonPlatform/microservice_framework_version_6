package uk.gov.justice.services.test.utils.core.random;


import static java.time.Instant.ofEpochMilli;
import static uk.gov.justice.services.test.utils.core.random.DateGenerator.Direction.FUTURE;

import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.LongStream;

public class ZonedDateTimeGenerator extends DateGenerator<ZonedDateTime> {

    private final ZonedDateTime start;
    private final Direction direction;
    private final ZonedDateTime end;

    public ZonedDateTimeGenerator(final Period period, final ZonedDateTime start, final Direction direction) {
        this.start = start;
        this.direction = direction;
        this.end = direction == FUTURE ? start.plus(period) : start.minus(period);
    }

    @Override
    public ZonedDateTime next() {
        final long startMillsecs = start.toInstant().toEpochMilli();
        final long endMillisecs = end.toInstant().toEpochMilli();

        final LongStream longStream;
        final Long randomMillsecs;

        if (direction == FUTURE) {
            longStream = RANDOM.longs(startMillsecs, endMillisecs);
        } else {
            longStream = RANDOM.longs(endMillisecs, startMillsecs);
        }

        randomMillsecs = longStream.limit(1).findFirst().getAsLong();
        return ofEpochMilli(randomMillsecs).atZone(ZoneId.systemDefault());
    }
}
