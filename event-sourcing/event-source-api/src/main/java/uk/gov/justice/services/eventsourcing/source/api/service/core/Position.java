package uk.gov.justice.services.eventsourcing.source.api.service.core;

public class Position {
    private static final int FIRST_SEQUENCE = 1;
    private final long position;
    private final boolean head;
    private final boolean first;

    public static Position head() {
        return new Position(-1L, true, false);
    }

    public static Position first() {
        return new Position(1L, false, true);
    }

    public static Position empty() {
        return new Position(1L, false, false);
    }

    public static Position position(final long position) {
        final boolean first = position == FIRST_SEQUENCE;
        return new Position(position, false, first);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position positionObject = (Position) o;

        if (position != positionObject.position) return false;
        if (head != positionObject.head) return false;
        return first == positionObject.first;
    }

    @Override
    public int hashCode() {
        int result = (int) (position ^ (position >>> 32));
        result = 31 * result + (head ? 1 : 0);
        result = 31 * result + (first ? 1 : 0);
        return result;
    }

    private Position(final long position, final boolean head, final boolean first) {
        this.position = position;
        this.head = head;
        this.first = first;
    }

    public long getPosition() {
        return position;
    }

    public boolean isHead() {
        return head;
    }

    public boolean isFirst() {
        return first;
    }

}
