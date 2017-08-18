package uk.gov.justice.services.eventsourcing.source.api.feed.common;

public class Paging {

    private final String previous;
    private final String next;

    public Paging(final String previous, final String next) {
        this.previous = previous;
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public String getNext() {
        return next;
    }
}
