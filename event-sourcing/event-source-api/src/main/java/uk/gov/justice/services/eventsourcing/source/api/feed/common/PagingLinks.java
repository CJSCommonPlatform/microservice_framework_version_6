package uk.gov.justice.services.eventsourcing.source.api.feed.common;

import java.net.URL;
import java.util.Optional;

public class PagingLinks {

    private Optional<URL> previous;
    private Optional<URL> next;
    private final URL head;
    private final URL first;

    private PagingLinks(final PagingLinksBuilder builder) {
        this.head = builder.head;
        this.first = builder.first;
        this.previous = builder.previous;
        this.next = builder.next;
    }

    public Optional<URL> getPrevious() {
        return previous;
    }

    public Optional<URL> getNext() {
        return next;
    }

    public URL getHead() {
        return head;
    }

    public URL getFirst() {
        return first;
    }

    public static class PagingLinksBuilder {

        private final URL head;
        private final URL first;

        private Optional<URL> previous = Optional.empty();
        private Optional<URL> next = Optional.empty();

        public PagingLinksBuilder(final URL head, final URL first) {
            this.first = first;
            this.head = head;
        }

        public PagingLinksBuilder previous(final Optional<URL> previous) {
            this.previous = previous;
            return this;
        }

        public PagingLinksBuilder next(final Optional<URL> next) {
            this.next = next;
            return this;
        }

        public PagingLinks build() {
            return new PagingLinks(this);
        }

    }
}
