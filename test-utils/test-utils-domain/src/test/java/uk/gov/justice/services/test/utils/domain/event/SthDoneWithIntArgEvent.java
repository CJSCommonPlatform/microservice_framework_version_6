package uk.gov.justice.services.test.utils.domain.event;


import uk.gov.justice.domain.annotation.Event;

@Event("context.sth-done-with-int-arg")
public class SthDoneWithIntArgEvent {
    private final Integer intArg;

    public SthDoneWithIntArgEvent(final Integer intArg) {
        this.intArg = intArg;
    }

    public Integer getIntArg() {
        return intArg;
    }
}
