package uk.gov.justice.services.eventsourcing.source.api.feed.common;

import java.util.List;

public class Feed<T> {
    private final List<T> data;
    private final Paging paging;

    public Feed(final List<T> data, final Paging paging) {
        this.data = data;
        this.paging = paging;
    }

    public List<T> getData() {
        return data;
    }

    public Paging getPaging() {
        return paging;
    }
}
