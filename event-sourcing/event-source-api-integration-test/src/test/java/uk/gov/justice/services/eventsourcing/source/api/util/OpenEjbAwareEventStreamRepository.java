package uk.gov.justice.services.eventsourcing.source.api.util;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;

public class OpenEjbAwareEventStreamRepository extends EventStreamJdbcRepository{
    @Override
    protected String jndiName() {
        return "java:openejb/Resource/eventStore";
    }
}
