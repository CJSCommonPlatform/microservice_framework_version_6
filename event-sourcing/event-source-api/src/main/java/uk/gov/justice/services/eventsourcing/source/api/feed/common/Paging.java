package uk.gov.justice.services.eventsourcing.source.api.feed.common;

public class Paging {

    private final String previous;
    private final String next;
    private final String head;
    private final String last;

    public Paging(final String previous,
                  final String next,
                  final String head,
                  final String last) {
        this.previous = previous;
        this.next = next;

        this.head = head;
        this.last = last;
    }

    public String getPrevious() {
        return previous;
    }

    public String getNext() {
        return next;
    }

    public String getHead() {
        return head;
    }

    public String getLast() {
        return last;
    }
}
