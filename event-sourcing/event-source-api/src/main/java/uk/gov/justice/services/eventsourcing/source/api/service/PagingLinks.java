package uk.gov.justice.services.eventsourcing.source.api.service;

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

        private URL head;
        private URL first;

        private Optional<URL> previous = Optional.empty();
        private Optional<URL> next = Optional.empty();

        public static PagingLinksBuilder pagingLinksBuilder(final URL head, final URL first) {
            final PagingLinksBuilder pagingLinksBuilder = new PagingLinksBuilder();
            pagingLinksBuilder.head = head;
            pagingLinksBuilder.first = first;

            return pagingLinksBuilder;
        }

        public PagingLinksBuilder withPrevious(final Optional<URL> previous) {
            this.previous = previous;
            return this;
        }

        public PagingLinksBuilder withNext(final Optional<URL> next) {
            this.next = next;
            return this;
        }

        public PagingLinks build() {
            return new PagingLinks(this);
        }

    }
}
