package uk.gov.justice.services.eventsourcing.source.api.service;

import java.util.List;

public class Page<T> {
    private final List<T> data;
    private final PagingLinks pagingLinks;

    public Page(final List<T> data, final PagingLinks pagingLinks) {
        this.data = data;
        this.pagingLinks = pagingLinks;
    }

    public List<T> getData() {
        return data;
    }

    public PagingLinks getPagingLinks() {
        return pagingLinks;
    }
}
