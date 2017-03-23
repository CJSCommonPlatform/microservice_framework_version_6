package uk.gov.justice.services.core.accesscontrol;

import uk.gov.justice.services.common.exception.ForbiddenRequestException;

public class AccessControlViolationException extends ForbiddenRequestException {

    public AccessControlViolationException(final String message) {
        super(message);
    }
}
