package uk.gov.justice.services.eventsourcing.source.api.feed.common;

import uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition;
import uk.gov.justice.services.eventsourcing.repository.jdbc.Position;

import org.mockito.ArgumentMatcher;

public class PositionArgumentMatcher extends ArgumentMatcher<Position> {
    long sequenceId;
    String fixedPosition;

    public PositionArgumentMatcher(final String fixedPosition) {
        this.fixedPosition = fixedPosition;
    }

    public PositionArgumentMatcher(final long sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public String toString() {
        return "[Position with sequenceId " + sequenceId + "]";
    }

    @Override
    public boolean matches(Object o) {
        if (o instanceof FixedPosition) {
            return ((Position) o).getPosition().equals(fixedPosition);

        }
        return ((Position) o).getSequenceId() == sequenceId;
    }
}
