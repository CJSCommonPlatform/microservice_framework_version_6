package uk.gov.justice.services.test.utils.core.random;


import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class LocalDateGenerator implements Generator<LocalDate> {
    private final LocalDate start;
    private final LocalDate end;
    private final Direction direction;

    public LocalDateGenerator(Period period, LocalDate start, Direction direction) {
        this.start = start;
        this.direction = direction;
        this.end = direction == Direction.FORWARD ? start.plus(period) : start.minus(period);
    }

    @Override
    public LocalDate next() {
        ZonedDateTime startZDT = start.atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime endZDT = end.atStartOfDay(ZoneOffset.UTC);
        long startMillsecs = startZDT.toInstant().toEpochMilli();
        long endMillisecs = endZDT.toInstant().toEpochMilli();

        Long randomMillsecs;

        if (direction == Direction.FORWARD) {
            randomMillsecs = RANDOM.longs(startMillsecs, endMillisecs).findFirst().getAsLong();
        } else {
            randomMillsecs = RANDOM.longs(endMillisecs, startMillsecs).findFirst().getAsLong();
        }

        return Instant.ofEpochMilli(randomMillsecs).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static enum Direction {FORWARD, BACKWARD}
}
