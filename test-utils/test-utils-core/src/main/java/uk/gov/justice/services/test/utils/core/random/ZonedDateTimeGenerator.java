package uk.gov.justice.services.test.utils.core.random;


import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneOffset.ofTotalSeconds;
import static uk.gov.justice.services.test.utils.core.random.DateGenerator.Direction.FUTURE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;

import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.LongStream;

public class ZonedDateTimeGenerator extends DateGenerator<ZonedDateTime> {

    private static final int SECONDS_PER_HOUR = 3600;
    private static final int MAX_ZONE_OFFSET_SECONDS = 18 * SECONDS_PER_HOUR;

    private final ZonedDateTime start;
    private final Direction direction;
    private final ZonedDateTime end;
    private final ZoneOffset zoneOffset;

    public ZonedDateTimeGenerator(final Period period, final ZonedDateTime start, final Direction direction) {
        this(period, start, direction, null);
    }

    public ZonedDateTimeGenerator(final Period period, final ZonedDateTime start, final Direction direction,
                                  final ZoneOffset zoneOffset) {
        this.start = start;
        this.direction = direction;
        this.end = direction == FUTURE ? start.plus(period) : start.minus(period);
        this.zoneOffset = zoneOffset;
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

        randomMillsecs = longStream.limit(1).findFirst().orElse(BOOLEAN.next() ? startMillsecs : endMillisecs);
        return ofEpochMilli(randomMillsecs).atZone(zoneOffset != null ? zoneOffset : randomZoneOffset());
    }

    private ZoneOffset randomZoneOffset() {
        return ofTotalSeconds(integer(-MAX_ZONE_OFFSET_SECONDS, MAX_ZONE_OFFSET_SECONDS).next());
    }
}
