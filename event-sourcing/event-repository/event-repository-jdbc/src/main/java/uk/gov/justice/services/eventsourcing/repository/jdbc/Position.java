package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static java.lang.Long.valueOf;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition.FIRST;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition.HEAD;

public class Position {

    private boolean isHead = false;

    private boolean isFirst = false;

    private long sequenceId;

    public static Position positionOf(final String sequenceIdStr) {
        if (HEAD.getPosition().equals(sequenceIdStr)) {
            return head();
        }
        if (FIRST.getPosition().equals(sequenceIdStr)) {
            return first();
        }
        return positionOf(valueOf(sequenceIdStr));
    }

    public static Position positionOf(final long sequenceId) {
        return new Position(sequenceId);
    }

    public static Position head() {
        return new Position(HEAD);
    }

    public static Position first() {
        return new Position(FIRST);
    }

    public String getPosition() {
        if (isHead()) {
            return HEAD.getPosition();
        }
        if (isFirst()) {
            return FIRST.getPosition();
        }
        return String.valueOf(getSequenceId());
    }

    public boolean isHead() {
        return isHead;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    private Position(final long sequenceId) {
        this.sequenceId = sequenceId;
    }

    private Position(final FixedPosition fixedPosition) {
        if (HEAD.getPosition().equals(fixedPosition.getPosition())) {
            isHead = true;
        }
        if (FIRST.getPosition().equals(fixedPosition.getPosition())) {
            isFirst = true;
        }
    }
}
