package uk.gov.justice.services.adapter.rest.exception;

import java.util.UUID;

public class ConflictedResourceException extends RuntimeException {


    private final UUID conflictingId;

    public ConflictedResourceException(final String message, final UUID conflictingId) {
        super(message);
        this.conflictingId = conflictingId;
    }

    public ConflictedResourceException(final String message, final Throwable cause, final UUID conflictingId) {
        super(message, cause);
        this.conflictingId = conflictingId;
    }

    /**
     * Returns the id of the resource that caused the conflict.
     *
     * @return the id (as a {@link UUID} of the conflicting resource.
     */
    public UUID getConflictingId() {
        return conflictingId;
    }
}
