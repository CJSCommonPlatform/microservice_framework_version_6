package uk.gov.justice.services.core.accesscontrol;

public class AccessControlViolation {

    private final String reason;

    public AccessControlViolation(final String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
