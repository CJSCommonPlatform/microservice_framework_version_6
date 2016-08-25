package uk.gov.justice.services.eventsourcing.common.exception;

public class DuplicateSnapshotException extends Throwable {
    private static final long serialVersionUID = 5934757852541630746L;

    public DuplicateSnapshotException(String message) {
        super(message);
    }

}

