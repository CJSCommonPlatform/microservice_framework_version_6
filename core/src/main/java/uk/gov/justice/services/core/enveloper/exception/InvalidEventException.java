package uk.gov.justice.services.core.enveloper.exception;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.core.enveloper.Enveloper;

/**
 * Exception thrown when {@link Enveloper} receives an invalid {@link Event} object.
 */
public class InvalidEventException extends RuntimeException {

    private static final long serialVersionUID = 4943839098543355539L;

    public InvalidEventException(final String message) {
        super(message);
    }

}
