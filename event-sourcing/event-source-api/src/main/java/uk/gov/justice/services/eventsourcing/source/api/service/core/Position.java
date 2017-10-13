package uk.gov.justice.services.eventsourcing.source.api.service.core;

public class Position {
    private static final int FIRST_SEQUENCE = 1;
    private final long sequenceId;
    private final boolean head;
    private final boolean first;

    public static Position head() {
        return new Position(-1L, true, false);
    }

    public static Position first() {
        return new Position(1L, false, true);
    }

    public static Position sequence(final long sequenceId) {
        final boolean first = sequenceId == FIRST_SEQUENCE;
        return new Position(sequenceId, false, first);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (sequenceId != position.sequenceId) return false;
        if (head != position.head) return false;
        return first == position.first;
    }

    @Override
    public int hashCode() {
        int result = (int) (sequenceId ^ (sequenceId >>> 32));
        result = 31 * result + (head ? 1 : 0);
        result = 31 * result + (first ? 1 : 0);
        return result;
    }

    private Position(final long sequenceId, final boolean head, final boolean first) {
        this.sequenceId = sequenceId;
        this.head = head;
        this.first = first;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public boolean isHead() {
        return head;
    }

    public boolean isFirst() {
        return first;
    }

}
