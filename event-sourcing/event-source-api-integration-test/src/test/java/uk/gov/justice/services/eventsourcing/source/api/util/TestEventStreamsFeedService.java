package uk.gov.justice.services.eventsourcing.source.api.util;

import uk.gov.justice.services.eventsourcing.source.api.service.EventStreamsFeedService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestEventStreamsFeedService extends EventStreamsFeedService {

    public void initialiseWithPageSize(final int pageSize) {
        this.pageSize = pageSize;
        this.initialise();
    }
}