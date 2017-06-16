package uk.gov.justice.services.test.utils.domain.event;


import uk.gov.justice.domain.annotation.Event;

@Event("context.sth-done-with-string-arg")
public class SthDoneWithStringArgEvent {
    private final String strArg;

    public SthDoneWithStringArgEvent(final String strArg) {
        this.strArg = strArg;
    }

    public String getStrArg() {
        return strArg;
    }
}
