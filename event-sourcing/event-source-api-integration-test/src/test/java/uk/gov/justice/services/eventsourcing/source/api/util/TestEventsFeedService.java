package uk.gov.justice.services.eventsourcing.source.api.util;

import uk.gov.justice.services.eventsourcing.source.api.service.EventsFeedService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestEventsFeedService extends EventsFeedService {

    public void initialiseWithPageSize() {
        this.initialise();
    }
}